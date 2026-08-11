package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity_;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.google.common.base.Strings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class DictionaryItemRepository extends BaseRepository<DictionaryItemEntity> {

    @Inject
    DictionaryMapper dictionaryMapper;

    public DictionaryItemRepository() {
        super(ELNEntityType.DICTIONARY_ITEM, DictionaryItemEntity.class);
    }

    public List<DictionaryItemEntity> listAll() {
        CriteriaDefinition<DictionaryItemEntity> criteria = new CriteriaDefinition<>(em, DictionaryItemEntity.class) {{
            JpaRoot<DictionaryItemEntity> root = from(DictionaryItemEntity.class);
            select(root);
            orderBy(asc(root.get(DictionaryItemEntity_.name)));
        }};
        return doFind(criteria, null, null);
    }

    public List<DictionaryItemEntity> list(UUID dictionaryID, boolean includeInactive) {
        CriteriaDefinition<DictionaryItemEntity> criteria = new CriteriaDefinition<>(em, DictionaryItemEntity.class) {{
            JpaRoot<DictionaryItemEntity> root = from(DictionaryItemEntity.class);
            select(root);
            List<Predicate> predicates = new ArrayList<>(List.of(
                    root.get(DictionaryItemEntity_.dictionary).get(DictionaryItemEntity_.id).equalTo(dictionaryID),
                    isFalse(root.get(DictionaryItemEntity_.deleted))
            ));
            if (!includeInactive) {
                predicates.add(isTrue(root.get(DictionaryItemEntity_.active)));
            }
            where(predicates);
            orderBy(asc(root.get(DictionaryItemEntity_.ordinal)));
        }};
        return doFind(criteria, null, null);
    }

    public List<DictionaryItemRef> suggest(UUID dictionaryID, @Nullable String search) {
        CriteriaDefinition<DictionaryItemEntity> criteria = new CriteriaDefinition<>(em, DictionaryItemEntity.class) {{
            JpaRoot<DictionaryItemEntity> root = from(DictionaryItemEntity.class);
            select(root);
            List<Predicate> predicates = new ArrayList<>(List.of(
                    root.get(DictionaryItemEntity_.dictionary).get(DictionaryItemEntity_.id).equalTo(dictionaryID),
                    isFalse(root.get(DictionaryItemEntity_.deleted)),
                    isTrue(root.get(DictionaryItemEntity_.active))
            ));
            if (!Strings.isNullOrEmpty(search)) {
                predicates.add(ilike(root.get(DictionaryItemEntity_.name), search.toLowerCase() + "%"));
            }
            where(predicates);
            orderBy(asc(root.get(DictionaryItemEntity_.name)));
        }};
        List<DictionaryItemEntity> list = doFind(criteria, Paging.DEFAULT, null);
        return map(list, dictionaryMapper::itemToRef);
    }
}
