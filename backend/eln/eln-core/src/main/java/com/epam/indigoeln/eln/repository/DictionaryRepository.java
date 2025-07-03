package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DictionaryRepository implements PanacheRepositoryBase<DictionaryItemEntity, UUID> {

    private static final Sort SORT = Sort.by("ordinal");

    public List<DictionaryItemEntity> list(Dictionary dictionary, boolean includeInactive) {
        Conditions conditions = new Conditions()
                .add("dictionary=?", dictionary);
        if (!includeInactive) {
            conditions.add("active");
        }
        return find(conditions.getQuery(), SORT, conditions.getValues()).list();
    }
}
