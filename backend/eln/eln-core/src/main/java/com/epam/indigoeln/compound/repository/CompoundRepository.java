package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.CompoundMapper;
import com.epam.indigoeln.compound.model.CompoundKey;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public class CompoundRepository extends BaseRepository<CompoundEntity> {

    @Inject
    CompoundMapper compoundMapper;

    public CompoundRepository() {
        super(ELNEntityType.COMPOUND, CompoundEntity.class);
    }

    @Nullable
    public CompoundEntity findByCompoundKey(CompoundKey compoundKey) {
        Conditions conditions = new Conditions()
                .add("canSmiles=?", compoundKey.getCanSmiles())
                .add("stereoisomerCode.id is not distinct from ?", compoundKey.getStereoisomerCode())
                .add("saltEQ100 is not distinct from ?", compoundKey.getSaltEQ100())
                .add("saltCode.id is not distinct from ?", compoundKey.getSaltCode());
        return doFindOne(conditions,
                null,
                Function.identity()
        );
    }

    @Nullable
    public STRCodeCompound findSameSTRCodeByCompoundKeyWithoutSaltCode(CompoundKey compoundKey) {
        return em.createQuery("select strCode from Compound "
                        + "where canSmiles = ?1 "
                        + "and stereoisomerCode.id is not distinct from ?2 "
                        + "and strCode is not null", STRCodeCompound.class)
                .setParameter(1, compoundKey.getCanSmiles())
                .setParameter(2, compoundKey.getStereoisomerCode())
                .setMaxResults(1)
                .getResultStream().findFirst().orElse(null);
    }


    public int getNextSTRCodeCompoundCode() {
        return (Integer) em.createNativeQuery("SELECT nextval('compound_str_code_compound_seq')", Integer.class)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, List.of("nothing")) // Hibernate assumes empty list as missing, so provide non-existent query space
                .getSingleResult();
    }
}
