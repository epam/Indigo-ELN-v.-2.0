package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.CompoundMapper;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.reaction.model.CompoundKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import org.jspecify.annotations.Nullable;

@ApplicationScoped
public class CompoundRepository extends BaseRepository<CompoundEntity> {

    @Inject
    CompoundMapper compoundMapper;

    public CompoundRepository() {
        super(CompoundEntity.class);
    }

    @Nullable
    public CompoundEntity findByCompoundKey(CompoundKey compoundKey) {
        TypedQuery<CompoundEntity> query = em.createQuery("""
                    from Compound where canSmiles=?1
                        and stereoisomerCode.id is not distinct from ?2
                        and saltEQ100 is not distinct from ?3
                        and saltCode.id is not distinct from ?4
                """, CompoundEntity.class);
        return query
                .setParameter(1, compoundKey.getCanSmiles())
                .setParameter(2, compoundKey.getStereoisomerCode())
                .setParameter(3, compoundKey.getSaltEQ100())
                .setParameter(4, compoundKey.getSaltCode())
                .getSingleResultOrNull();
    }
}
