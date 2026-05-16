package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.EntityType;
import com.google.common.base.Strings;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class DictionaryItemRepository extends BaseRepository<DictionaryItemEntity> {

    private static final Sort SORT = Sort.by("ordinal");
    private static final Sort SORT_SUGGEST = Sort.by("name");

    @Inject
    DictionaryMapper dictionaryMapper;

    public DictionaryItemRepository() {
        super(EntityType.DICTIONARY_ITEM, DictionaryItemEntity.class);
    }

    public List<DictionaryItemEntity> list(UUID dictionaryID, boolean includeInactive) {
        Conditions conditions = new Conditions()
                .add("dictionary.id=?", dictionaryID)
                .add("not deleted");
        if (!includeInactive) {
            conditions.add("active");
        }
        return find(conditions.getQuery(), SORT, conditions.getValues()).list();
    }

    public Map<String, DictionaryItemEntity> findByNames(UUID dictionaryID, Collection<String> names) {
        Conditions conditions = new Conditions()
                .add("dictionary.id=?", dictionaryID)
                .add("not deleted")
                .add("name IN ?", names);
        return StreamEx.of(find(conditions.getQuery(), conditions.getValues()).stream())
                .toMap(DictionaryItemEntity::getName, item -> item);
    }

    public List<DictionaryItemRef> suggest(UUID dictionaryID, @Nullable String search) {
        Conditions conditions = new Conditions()
                .add("dictionary.id=?", dictionaryID)
                .add("not deleted")
                .add("active");
        if (!Strings.isNullOrEmpty(search)) {
            conditions.add("LOWER(name) LIKE ?", search.toLowerCase() + "%");
        }
        return doFind(conditions,
                Paging.DEFAULT,
                SORT_SUGGEST,
                null,
                dictionaryMapper::itemToRef
        );
    }
}
