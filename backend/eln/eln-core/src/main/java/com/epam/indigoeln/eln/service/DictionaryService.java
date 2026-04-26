package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.TraceSegment;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@DataAccess
@TraceSegment
@Transactional
@ApplicationScoped
public class DictionaryService {

    @Inject
    DictionaryRepository dictionaryRepository;

    @Inject
    DictionaryItemRepository dictionaryItemRepository;

    @Inject
    SaltCodeRepository saltCodeRepository;

    @Inject
    DictionaryMapper dictionaryMapper;

    @Inject
    ACLService aclService;

    @Inject
    UserService userService;

    @Inject
    @CacheName("dictionary.items")
    Cache dictionaryItemsCache;

    @PersistenceContext
    EntityManager em;

    public List<DictionaryDTO> getDictionaries() {
        return dictionaryRepository.list();
    }

    public DictionaryDTO createDictionary(DictionaryRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        DictionaryEntity dictionary = dictionaryMapper.requestToEntity(request);
        updateDates(dictionary, userService.getCurrentUserEntity());
        dictionaryRepository.persist(dictionary);
        dictionaryRepository.flushAndRefresh(dictionary);
        return dictionaryMapper.dictionaryToDTO(dictionary);
    }

    public DictionaryDTO updateDictionary(String dictionaryRef, DictionaryEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        DictionaryEntity dictionary = dictionaryRepository.get(refToID(dictionaryRef));
        InvalidRequestException.validate(!dictionary.getDeleted(), "Dictionary is deleted");
        editProperty(request.getCode(), dictionary::setCode);
        editProperty(request.getName(), dictionary::setName);
        editProperty(request.getUserEditable(), dictionary::setUserEditable);
        editProperty(request.getDescription(), dictionary::setDescription);
        return dictionaryMapper.dictionaryToDTO(dictionary);
    }

    public void removeDictionary(String dictionaryRef) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        DictionaryEntity dictionary = dictionaryRepository.get(refToID(dictionaryRef));
        InvalidRequestException.validate(!dictionary.getDeleted(), "Dictionary is deleted");
        dictionary.setDeleted(true);
    }

    public List<DictionaryItemRef> getDictionary(String dictionaryRef) {
        return dictionaryMapper.itemToRefList(dictionaryItemRepository.list(refToID(dictionaryRef), false));
    }

    public List<DictionaryItemDTO> getDictionaryFull(String dictionaryRef) {
        return dictionaryMapper.itemToDTOList(dictionaryItemRepository.list(refToID(dictionaryRef), true));
    }

    @Nullable
    public DictionaryItemEntity lookup(String dictionaryRef, @Nullable DictionaryItemRef ref) {
        if (ref == null) {
            return null;
        }
        DictionaryItemRef item = doGetDictionaryItems(refToID(dictionaryRef)).get(ref.getId());
        if (item == null) {
            throw new EntityNotFoundException(EntityType.DICTIONARY_ITEM, ref.getId() + " in dictionary " + dictionaryRef);
        }
        return dictionaryItemRepository.getReference(ref.getId());
    }

    public List<DictionaryItemEntity> lookup(String dictionaryRef, Collection<DictionaryItemRef> refs) {
        Map<UUID, DictionaryItemRef> allItems = doGetDictionaryItems(refToID(dictionaryRef));
        List<DictionaryItemEntity> found = new ArrayList<>(refs.size());
        List<UUID> notFound = new ArrayList<>(refs.size());
        for (DictionaryItemRef ref : refs) {
            if (allItems.containsKey(ref.getId())) {
                found.add(dictionaryItemRepository.getReference(ref.getId()));
            } else {
                notFound.add(ref.getId());
            }
        }
        if (!notFound.isEmpty()) {
            throw new EntityNotFoundException(EntityType.DICTIONARY_ITEM, notFound + " in dictionary " + dictionaryRef);
        }
        return found;
    }

    @CacheResult(cacheName = "dictionary.items")
    protected Map<UUID, DictionaryItemRef> doGetDictionaryItems(UUID dictionaryID) {
        return StreamEx.of(dictionaryItemRepository.list(dictionaryID, true))
                .mapToEntry(DictionaryItemEntity::getId, dictionaryMapper::itemToRef)
                .toCustomMap(LinkedHashMap::new);
    }

    public List<DictionaryItemRef> suggestDictionaryItems(String dictionaryRef, String search) {
        return dictionaryItemRepository.suggest(refToID(dictionaryRef), search);
    }

    // TODO use cache
    public DictionaryItemEntity get(UUID id) {
        return dictionaryItemRepository.findById(id);
    }

    @Nullable
    public DictionaryItemEntity get(@Nullable DictionaryItemRef ref) {
        return ref != null ? dictionaryItemRepository.get(ref.getId()) : null;
    }

    public List<DictionaryItemRef> getSaltCodes() {
        return dictionaryMapper.saltCodeToRefList(saltCodeRepository.listAll());
    }

    public SaltCodeEntity getSalt(UUID id) {
        return saltCodeRepository.findById(id);
    }

    @CacheResult(cacheName = "dictionary.saltCodes")
    public SaltCodeInfo getSaltInfo(UUID id) {
        return dictionaryMapper.saltCodeToInfo(getSalt(id));
    }

    public DictionaryItemRef getSaltRef(UUID id) {
        return getSaltInfo(id).toRef();
    }

    public List<DictionaryItemEntity> addDictionaryItems(String dictionaryRef, List<DictionaryItemRequest> items) {
        DictionaryEntity dictionary = dictionaryRepository.findById(refToID(dictionaryRef));
        if (!dictionary.getUserEditable()) {
            aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        }
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        List<DictionaryItemEntity> inserted = new ArrayList<>(items.size());
        for (DictionaryItemRequest item : items) {
            DictionaryItemEntity entity = dictionaryMapper.itemToEntity(item);
            entity.setDictionary(dictionary);
            updateDates(entity, userService.getCurrentUserEntity());
            inserted.add(entity);
        }
        list.addAll(inserted);
        renumberItems(list, true);
        em.flush();
        renumberItems(list, false);
        dictionaryItemRepository.persist(inserted);
        dictionaryItemsCache.invalidate(refToID(dictionaryRef)).await().indefinitely();
        return list;
    }

    public List<DictionaryItemDTO> updateDictionaryItem(String dictionaryRef, UUID itemID, DictionaryItemEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(EntityType.DICTIONARY, itemID + " of dictionary " + dictionaryRef));
        editProperty(request.getName(), entity::setName);
        editProperty(request.getDescription(), entity::setDescription);
        editProperty(request.getActive(), entity::setActive);
        editProperty(request.getOrdinal(), order -> {
            // assign items negative numbers first, to avoid unique index violations;
            // if we had unique constraint, we could use deferred constraint, but we have to use partial unique index to cover only non-deleted items
            renumberItems(list, true);
            em.flush();
            // reorder items, move item to new position and reorder again
            renumberItems(list, false);
            list.remove(entity);
            list.add(order - 1, entity);
            renumberItems(list, false);
        });
        dictionaryItemsCache.invalidate(refToID(dictionaryRef)).await().indefinitely();
        return dictionaryMapper.itemToDTOList(list);
    }

    public List<DictionaryItemDTO> removeDictionaryItem(String dictionaryRef, UUID itemID) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(EntityType.DICTIONARY_ITEM, itemID + " of dictionary " + dictionaryRef));
        list.remove(entity);
        try {
            entity.setDeleted(true);
        } catch (ConstraintViolationException e) {
            log.error("Failed to delete dictionary item {}", itemID, e);
            throw new InvalidRequestException("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
        }
        renumberItems(list, true);
        em.flush();
        renumberItems(list, false);
        dictionaryItemsCache.invalidate(refToID(dictionaryRef)).await().indefinitely();
        return dictionaryMapper.itemToDTOList(list);
    }

    public List<DictionaryItemEntity> findOrCreateByNames(String dictionaryRef, Collection<String> names) {
        DictionaryEntity dictionary = dictionaryRepository.findById(refToID(dictionaryRef));
        if (!dictionary.getUserEditable()) {
            throw new IllegalArgumentException("findOrCreateByNames cannot be used with dictionary " + dictionary);
        }
        List<DictionaryItemEntity> result = new ArrayList<>(names.size());
        Map<String, DictionaryItemEntity> found = dictionaryItemRepository.findByNames(dictionary.getId(), names);
        if (found.size() < names.size()) {
            Set<String> remainingNames = new HashSet<>(names);
            remainingNames.removeAll(found.keySet());
            List<DictionaryItemEntity> newAllItems = addDictionaryItems(dictionaryRef, remainingNames.stream().map(x -> new DictionaryItemRequest(x, null)).toList());
            found = StreamEx.of(newAllItems).toMap(DictionaryItemEntity::getName, x -> x);
        }
        dictionaryItemsCache.invalidate(refToID(dictionaryRef)).await().indefinitely();
        return StreamEx.of(names).map(found::get).toList();
    }

    private void renumberItems(List<DictionaryItemEntity> items, boolean negative) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrdinal((i + 1) * (negative ? -1 : 1));
        }
    }

    private UUID refToID(String dictionaryRef) {
        try {
            return UUID.fromString(dictionaryRef);
        } catch (IllegalArgumentException e) {
            try {
                return BuiltInDictionary.valueOf(dictionaryRef).getId();
            } catch (IllegalArgumentException ex) {
                throw new EntityNotFoundException(EntityType.DICTIONARY, dictionaryRef);
            }
        }
    }
}
