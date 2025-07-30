package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
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
    SaltCodeRepository saltCodeRepository;

    @Inject
    DictionaryMapper dictionaryMapper;

    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    public List<Dictionary> getDictionaries() {
        return StreamEx.of(Dictionary.values()).remove(x -> x == Dictionary.TEST).toList();
    }

    public List<DictionaryItemRef> getDictionary(Dictionary dictionary) {
        return dictionaryMapper.dictionaryToRefList(dictionaryRepository.list(dictionary, false));
    }

    public List<DictionaryItemDTO> getDictionaryFull(Dictionary dictionary) {
        return dictionaryMapper.dictionaryToDTOList(dictionaryRepository.list(dictionary, true));
    }

    public @Nullable DictionaryItemEntity lookup(Dictionary dictionary, @Nullable DictionaryItemRef ref) {
        if (ref == null) {
            return null;
        }
        DictionaryItemEntity entity = dictionaryRepository.findById(ref.getId());
        if (entity == null || entity.getDictionary() != dictionary) {
            throw new EntityNotFoundException(dictionary, ref.getId());
        }
        return entity;
    }

    public List<DictionaryItemRef> suggestDictionaryItems(Dictionary dictionary, String search) {
        return dictionaryRepository.suggest(dictionary, search);
    }

    public DictionaryItemEntity get(UUID id) {
        return dictionaryRepository.findById(id);
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

    public List<DictionaryItemEntity> addDictionaryItems(Dictionary dictionary, List<DictionaryItemRequest> items) {
        if (!dictionary.isUsersCanAddNewItems()) {
            aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        }
        List<DictionaryItemEntity> list = dictionaryRepository.list(dictionary, true);
        List<DictionaryItemEntity> inserted = new ArrayList<>(items.size());
        for (DictionaryItemRequest item : items) {
            DictionaryItemEntity entity = dictionaryMapper.dictionaryToEntity(item, dictionary);
            updateDates(entity, userService.getCurrentUser());
            inserted.add(entity);
        }
        list.addAll(inserted);
        renumberItems(list);
        dictionaryRepository.persist(inserted);
        return list;
    }

    public List<DictionaryItemDTO> updateDictionaryItem(Dictionary dictionary, UUID itemID, DictionaryItemEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryRepository.list(dictionary, true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(dictionary, itemID));
        editProperty(request.getName(), entity::setName);
        editProperty(request.getDescription(), entity::setDescription);
        editProperty(request.getActive(), entity::setActive);
        editProperty(request.getOrdinal(), order -> {
            renumberItems(list);
            list.remove(entity);
            list.add(order - 1, entity);
            renumberItems(list);
        });
        return dictionaryMapper.dictionaryToDTOList(list);
    }

    public List<DictionaryItemDTO> removeDictionaryItem(Dictionary dictionary, UUID itemID) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_DICTIONARIES);
        List<DictionaryItemEntity> list = dictionaryRepository.list(dictionary, true);
        DictionaryItemEntity entity = StreamEx.of(list).filterBy(DictionaryItemEntity::getId, itemID).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(dictionary, itemID));
        list.remove(entity);
        dictionaryRepository.delete(entity);
        renumberItems(list);
        return dictionaryMapper.dictionaryToDTOList(list);
    }

    public List<DictionaryItemEntity> findOrCreateByNames(Dictionary dictionary, Collection<String> names) {
        if (!dictionary.isUsersCanAddNewItems()) {
            throw new IllegalArgumentException("findOrCreateByNames cannot be used with dictionary " + dictionary);
        }
        List<DictionaryItemEntity> result = new ArrayList<>(names.size());
        Map<String, DictionaryItemEntity> found = dictionaryRepository.findByNames(dictionary, names);
        if (found.size() < names.size()) {
            Set<String> remainingNames = new HashSet<>(names);
            remainingNames.removeAll(found.keySet());
            List<DictionaryItemEntity> newAllItems = addDictionaryItems(dictionary, remainingNames.stream().map(x -> new DictionaryItemRequest(x, null)).toList());
            found = StreamEx.of(newAllItems).toMap(DictionaryItemEntity::getName, x -> x);
        }
        return StreamEx.of(names).map(found::get).toList();
    }

    private void renumberItems(List<DictionaryItemEntity> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrdinal(i + 1);
        }
    }
}
