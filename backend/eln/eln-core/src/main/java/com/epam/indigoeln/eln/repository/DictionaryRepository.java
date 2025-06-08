package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DictionaryRepository implements PanacheRepositoryBase<DictionaryEntity, UUID> {

    private static final Sort SORT = Sort.by("ordinal");

    public List<DictionaryEntity> list(Dictionary dictionary, boolean includeDeleted) {
        Conditions conditions = new Conditions()
                .add("dictionary=?", dictionary);
        if (!includeDeleted) {
            conditions.add("not deleted");
        }
        return find(conditions.getQuery(), SORT, conditions.getValues()).list();
    }

    public long hardDelete() {
        return delete("deleted");
    }
}
