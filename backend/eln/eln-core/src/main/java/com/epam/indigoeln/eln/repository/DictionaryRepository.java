package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.EntityType;
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
        super(EntityType.DICTIONARY, DictionaryEntity.class);
    }

    public List<DictionaryDTO> list() {
        return doFind(new Conditions().add("not deleted"), Paging.ALL, SORT_NAME, null, dictionaryMapper::dictionaryToDTO);
    }
}
