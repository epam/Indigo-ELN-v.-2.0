package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DictionaryRepository extends BaseRepository<DictionaryEntity> {

    private static final Sort SORT_NAME = Sort.by("name");

    @Inject
    DictionaryMapper dictionaryMapper;

    public DictionaryRepository() {
        super(EntityType.DICTIONARY);
    }

    public List<DictionaryDTO> list() {
        return doFind(new Conditions(), Paging.ALL, SORT_NAME, null, dictionaryMapper::dictionaryToDTO);
    }
}
