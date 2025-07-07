package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.CompoundMapper;
import com.epam.indigoeln.compound.model.CompoundKey;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.repository.BaseRepository;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@ApplicationScoped
public class CompoundRepository extends BaseRepository<CompoundEntity> {

    @Inject
    CompoundMapper compoundMapper;
    @PersistenceContext
    EntityManager em;

    public CompoundRepository() {
        super(EntityType.COMPOUND);
    }

    public @Nullable CompoundEntity findByCompoundKey(CompoundKey compoundKey, boolean useSaltCode) {
        Conditions conditions = new Conditions()
                .add("canSmiles=?", compoundKey.getCanSmiles())
                .addIfNotNull("stereoisomerCode=?", compoundKey.getStereoisomerCode())
                .addIf(compoundKey.getStereoisomerCode() == null, "stereoisomerCode is null")
                .addIfNotNull("saltEQ100=?", compoundKey.getSaltEQ100())
                .addIf(compoundKey.getSaltEQ100() == null, "saltEQ100 is null");
        if (useSaltCode) {
            conditions
                    .addIfNotNull("saltCode.id=?", compoundKey.getSaltCode())
                    .addIf(compoundKey.getSaltCode() == null, "saltCode is null");
        }
        return doFindOne(conditions,
                null,
                Function.identity()
        );
    }

    public int getNextSTRCodeCompoundCode() {
        return (Integer) em.createNativeQuery("SELECT nextval('compound_str_code_compound_seq')", Integer.class).getSingleResult();
    }
}
