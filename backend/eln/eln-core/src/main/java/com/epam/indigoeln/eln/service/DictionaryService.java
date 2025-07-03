package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.DictionaryNotFoundException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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

    public List<Dictionary> getDictionaries() {
        return Arrays.asList(Dictionary.values());
    }

    public List<DictionaryRef> getDictionary(Dictionary dictionary) {
        return dictionaryMapper.dictionaryToRefList(dictionaryRepository.list(dictionary, false));
    }

    public List<DictionaryDTO> getDictionaryFull(Dictionary dictionary) {
        return dictionaryMapper.dictionaryToDTOList(dictionaryRepository.list(dictionary, false));
    }

    public List<DictionaryDTO> updateDictionary(Dictionary dictionary, @Valid List<DictionaryRequest> content) {
        if (log.isDebugEnabled()) {
            log.debug("updateDictionary: before update:\n{}", StreamEx.of(dictionaryRepository.list(dictionary, true)).joining("\n"));
        }
        aclService.ensureTopLevelAccess(AccessOperation.MANAGE_DICTIONARIES);
        Map<UUID, DictionaryEntity> existing = StreamEx.of(dictionaryRepository.list(dictionary, true))
                .toMap(IdentifiableEntity::getId, Function.identity());
        int ordinal = 0;
        for (DictionaryRequest request : content) {
            DictionaryEntity entity;
            if (request.getId() != null) {
                entity = existing.remove(request.getId());
                if (entity == null) {
                    throw new EntityNotFoundException(dictionary, request.getId());
                }
                dictionaryMapper.dictionaryToEntity(request, dictionary, ++ordinal, entity);
            } else {
                entity = dictionaryMapper.dictionaryToEntity(request, dictionary, ++ordinal, new DictionaryEntity());
                dictionaryRepository.persist(entity);
            }
        }
        for (DictionaryEntity value : existing.values()) {
            value.setOrdinal(++ordinal);
            value.setDeleted(true);
        }
        if (log.isDebugEnabled()) {
            log.debug("updateDictionary: after update:\n{}", StreamEx.of(dictionaryRepository.list(dictionary, true)).joining("\n"));
        }
        long hardDeleted = dictionaryRepository.hardDelete();
        log.debug("updateDictionary: {} entries hard deleted", hardDeleted);
        return getDictionaryFull(dictionary);
    }

    public @Nullable DictionaryEntity lookup(Dictionary dictionary, @Nullable DictionaryRef ref) {
        if (ref == null) {
            return null;
        }
        DictionaryEntity entity = dictionaryRepository.findById(ref.getId());
        if (entity == null || entity.getDictionary() != dictionary) {
            throw new EntityNotFoundException(dictionary, ref.getId());
        }
        return entity;
    }

    public List<DictionaryRef> getSaltCodes() {
        return dictionaryMapper.saltCodeToRefList(saltCodeRepository.listAll());
    }
}
