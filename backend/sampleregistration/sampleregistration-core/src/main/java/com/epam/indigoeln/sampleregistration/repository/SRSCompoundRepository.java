package com.epam.indigoeln.sampleregistration.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.reaction.model.CompoundKey;
import com.epam.indigoeln.sampleregistration.entity.SRSCompoundEntity;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApplicationScoped
public class SRSCompoundRepository extends BaseRepository<SRSCompoundEntity> {

    public SRSCompoundRepository() {
        super(SRSCompoundEntity.class);
    }

    @Nullable
    public SRSCompoundEntity findByCompoundKey(CompoundKey compoundKey) {
        TypedQuery<SRSCompoundEntity> query = em.createQuery("""
                    from SRSCompound where canSmiles=?1
                        and stereoisomerCode is not distinct from ?2
                        and saltEQ100 is not distinct from ?3
                        and saltCode is not distinct from ?4
                """, SRSCompoundEntity.class);
        return query
                .setParameter(1, compoundKey.getCanSmiles())
                .setParameter(2, compoundKey.getStereoisomerCode())
                .setParameter(3, compoundKey.getSaltEQ100())
                .setParameter(4, compoundKey.getSaltCode())
                .getSingleResultOrNull();
    }

    @Nullable
    public STRCodeCompound findSameSTRCodeByCompoundKeyWithoutSaltCode(CompoundKey compoundKey) {
        TypedQuery<STRCodeCompound> query = em.createQuery("""
                    select strCode from SRSCompound
                    where canSmiles = ?1
                        and stereoisomerCode is not distinct from ?2
                        and strCode is not null
                """, STRCodeCompound.class);
        return query
                .setParameter(1, compoundKey.getCanSmiles())
                .setParameter(2, compoundKey.getStereoisomerCode())
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    public int getNextSTRCodeCompoundCode() {
        return (Integer) em.createNativeQuery("SELECT nextval('srs_compound_str_code_compound_seq')", Integer.class)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, List.of("nothing")) // Hibernate assumes empty list as missing, so provide non-existent query space
                .getSingleResult();
    }
}
