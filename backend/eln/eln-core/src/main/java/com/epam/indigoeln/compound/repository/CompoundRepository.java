package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.CompoundMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

@ApplicationScoped
public class CompoundRepository extends BaseRepository<CompoundEntity> {

    @Inject
    CompoundMapper compoundMapper;

    public CompoundRepository() {
        super(EntityType.COMPOUND);
    }

    public @Nullable CompoundEntity findByCanonicalSmiles(String canonicalSmiles) {
        return find("canonicalSmiles", canonicalSmiles).firstResult();
    }
}
