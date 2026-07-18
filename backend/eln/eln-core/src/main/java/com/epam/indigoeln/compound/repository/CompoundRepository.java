package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.CompoundMapper;
import com.epam.indigoeln.compound.model.CompoundKey;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApplicationScoped
public class CompoundRepository extends BaseRepository<CompoundEntity> {

    @Inject
    CompoundMapper compoundMapper;
    @PersistenceContext
    EntityManager em;

    public CompoundRepository() {
        super(ELNEntityType.COMPOUND, CompoundEntity.class);
    }

    @Nullable
    public CompoundEntity findByCompoundKey(CompoundKey compoundKey) {
        TypedQuery<CompoundEntity> query = em.createQuery("""
                    from Compound where canSmiles=?1 
                        and stereoisomerCode.id is not distinct from ?2
                        and saltEQ100 is not distinct from ?3
                        and saltCode.id is not distinct from ?4
                """, CompoundEntity.class);
        try {
            return query
                    .setParameter(1, compoundKey.getCanSmiles())
                    .setParameter(2, compoundKey.getStereoisomerCode())
                    .setParameter(3, compoundKey.getSaltEQ100())
                    .setParameter(4, compoundKey.getSaltCode())
                    .getSingleResultOrNull();
        } catch (NonUniqueResultException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Nullable
    public STRCodeCompound findSameSTRCodeByCompoundKeyWithoutSaltCode(CompoundKey compoundKey) {
        TypedQuery<STRCodeCompound> query = em.createQuery("""
                    select strCode from Compound
                    where canSmiles = ?1
                        and stereoisomerCode.id is not distinct from ?2
                        and strCode is not null
                """, STRCodeCompound.class);
        return query
                .setParameter(1, compoundKey.getCanSmiles())
                .setParameter(2, compoundKey.getStereoisomerCode())
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    public int getNextSTRCodeCompoundCode() {
        return (Integer) em.createNativeQuery("SELECT nextval('compound_str_code_compound_seq')", Integer.class)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, List.of("nothing")) // Hibernate assumes empty list as missing, so provide non-existent query space
                .getSingleResult();
    }
}
