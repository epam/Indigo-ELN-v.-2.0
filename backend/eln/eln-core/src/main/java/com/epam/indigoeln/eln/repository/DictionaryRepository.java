package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryEntity_;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class DictionaryRepository extends BaseRepository<DictionaryEntity> {

    @Inject
    DictionaryMapper dictionaryMapper;

    public DictionaryRepository() {
        super(ELNEntityType.DICTIONARY, DictionaryEntity.class);
    }

    public List<DictionaryDTO> list() {
        CriteriaDefinition<DictionaryEntity> criteria = new CriteriaDefinition<>(em, DictionaryEntity.class) {{
            JpaRoot<DictionaryEntity> root = from(DictionaryEntity.class);
            select(root);
            where(isFalse(root.get(DictionaryEntity_.deleted)));
            orderBy(asc(root.get(DictionaryEntity_.name)));
        }};
        List<DictionaryEntity> list = doFind(criteria, Paging.ALL, null);
        return map(list, dictionaryMapper::dictionaryToDTO);
    }

    public List<DictionaryEntity> listAll() {
        return findAll().list();
    }
}
