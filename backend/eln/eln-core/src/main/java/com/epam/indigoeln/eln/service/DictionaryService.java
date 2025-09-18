package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public List<DictionaryDTO> getDictionaries() {
        return dictionaryRepository.list();
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
        DictionaryItemEntity entity = dictionaryItemRepository.findById(ref.getId());
        if (entity == null || !entity.getDictionary().getId().equals(refToID(dictionaryRef))) {
            throw new EntityNotFoundException(EntityType.DICTIONARY_ITEM, ref.getId() + " in dictionary " + dictionaryRef);
        }
        return entity;
    }

    public List<DictionaryItemRef> suggestDictionaryItems(String dictionaryRef, String search) {
        return dictionaryItemRepository.suggest(refToID(dictionaryRef), search);
    }

    public DictionaryItemEntity get(UUID id) {
        return dictionaryItemRepository.findById(id);
    }

    public List<DictionaryItemRef> getSaltCodes() {
        return dictionaryMapper.saltCodeToRefList(saltCodeRepository.listAll());
    }

    public SaltCodeEntity getSalt(UUID id) {
        return saltCodeRepository.findById(id);
    }

    public SaltCodeRef getSaltRef(UUID id) {
        return dictionaryMapper.saltCodeToRef(getSalt(id));
    }

    public SaltCodeRef getSaltRef(SaltCodeEntity entity) {
        return dictionaryMapper.saltCodeToRef(entity);
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
            updateDates(entity, userService.getCurrentUser());
            inserted.add(entity);
        }
        list.addAll(inserted);
        renumberItems(list);
        dictionaryItemRepository.persist(inserted);
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
            renumberItems(list);
            list.remove(entity);
            list.add(order - 1, entity);
            renumberItems(list);
        });
        return dictionaryMapper.itemToDTOList(list);
    }

    public List<DictionaryItemDTO> removeDictionaryItem(String dictionaryRef, UUID itemID) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryItemRepository.list(refToID(dictionaryRef), true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(EntityType.DICTIONARY_ITEM, itemID + " of dictionary " + dictionaryRef));
        list.remove(entity);
        try {
            dictionaryItemRepository.delete(entity);
            dictionaryItemRepository.flush();
        } catch (ConstraintViolationException e) {
            log.error("Failed to delete dictionary item {}", itemID, e);
            throw new InvalidRequestException("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
        }
        renumberItems(list);
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
        return StreamEx.of(names).map(found::get).toList();
    }

    private void renumberItems(List<DictionaryItemEntity> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrdinal(i + 1);
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
