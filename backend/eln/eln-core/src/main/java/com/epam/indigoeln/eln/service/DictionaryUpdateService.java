package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hibernate.exception.ConstraintViolationException;

import java.util.*;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.service.DictionaryService.refToID;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@Transactional
@ApplicationScoped
public class DictionaryUpdateService {

    @Inject
    ACLService aclService;

    @Inject
    UserService userService;

    @Inject
    DictionaryService dictionaryService;

    @Inject
    DictionaryRepository dictionaryRepository;

    @Inject
    DictionaryItemRepository dictionaryItemRepository;

    @Inject
    DictionaryMapper dictionaryMapper;

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
        dictionaryService.invalidate();
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
        dictionaryService.invalidate();
        return dictionaryMapper.dictionaryToDTO(dictionary);
    }

    public void removeDictionary(String dictionaryRef) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        DictionaryEntity dictionary = dictionaryRepository.get(refToID(dictionaryRef));
        InvalidRequestException.validate(!dictionary.getDeleted(), "Dictionary is deleted");
        dictionaryService.invalidate();
        dictionary.setDeleted(true);
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
        dictionaryService.invalidate();
        return list;
    }

    public List<DictionaryItemDTO> updateDictionaryItem(String dictionaryRef, UUID itemID, DictionaryItemEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ELNEntityType.DICTIONARY, itemID + " of dictionary " + dictionaryRef));
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
        dictionaryService.invalidate();
        return dictionaryMapper.itemToDTOList(list);
    }

    public List<DictionaryItemDTO> removeDictionaryItem(String dictionaryRef, UUID itemID) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ELNEntityType.DICTIONARY_ITEM, itemID + " of dictionary " + dictionaryRef));
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
        dictionaryService.invalidate();
        return dictionaryMapper.itemToDTOList(list);
    }

    public List<DictionaryItemEntity> findOrCreateByNames(String dictionaryRef, Collection<String> names) {
        DictionaryEntity dictionary = dictionaryRepository.findById(refToID(dictionaryRef));
        if (!dictionary.getUserEditable()) {
            throw new IllegalArgumentException("findOrCreateByNames cannot be used with dictionary " + dictionary);
        }
        Map<String, DictionaryItemEntity> found = dictionaryItemRepository.findByNames(dictionary.getId(), names);
        if (found.size() < names.size()) {
            Set<String> remainingNames = new HashSet<>(names);
            remainingNames.removeAll(found.keySet());
            List<DictionaryItemEntity> newAllItems = addDictionaryItems(dictionaryRef, remainingNames.stream().map(x -> new DictionaryItemRequest(x, null)).toList());
            found = StreamEx.of(newAllItems).toMap(DictionaryItemEntity::getName, x -> x);
        }
        dictionaryService.invalidate();
        return StreamEx.of(names).map(found::get).toList();
    }

    private void renumberItems(List<DictionaryItemEntity> items, boolean negative) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrdinal((i + 1) * (negative ? -1 : 1));
        }
    }
}
