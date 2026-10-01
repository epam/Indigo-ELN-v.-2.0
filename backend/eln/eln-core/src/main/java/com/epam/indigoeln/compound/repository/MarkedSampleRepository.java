package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.CompoundEntity_;
import com.epam.indigoeln.compound.entity.MarkedSampleEntity;
import com.epam.indigoeln.compound.entity.MarkedSampleEntity_;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.util.ELNCriteriaConditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class MarkedSampleRepository extends BaseRepository<MarkedSampleEntity> {

    @Inject
    SampleMapper sampleMapper;
    @Inject
    ELNCriteriaConditions.Factory criteriaConditionsFactory;

    public MarkedSampleRepository() {
        super(MarkedSampleEntity.class);
    }

    public Page<SampleDTO> find(UserEntity user, FindSamplesRequest request, @Nullable Boolean marked, int pageNo, int pageSize) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<MarkedSampleEntity> root = from(MarkedSampleEntity.class);
            JpaJoin<MarkedSampleEntity, CompoundEntity> compound = root.join(MarkedSampleEntity_.compound); // will be optimized away if not used
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                orderBy(asc(root.get(MarkedSampleEntity_.id))); // default sort, can be overridden
                conditions.add(equal(root.get(MarkedSampleEntity_.user), user));
                conditions.fullTextSearch(root.get(MarkedSampleEntity_.searchVector), request.getQuickSearch(), null);

                conditions.moleculeSearch(compound.get(CompoundEntity_.molFile), request.getStructure());
                if (request.getStructure() != null && request.getStructure().type() != StructuralSearch.Type.EXACT) {
                    orderBy(desc(conditions.moleculeSimilarity(compound.get(CompoundEntity_.molFile), request.getStructure().query())));
                }

                conditions.textSearch(compound.get(CompoundEntity_.compoundKey), request.getCompoundKey());
                conditions.textSearch(root.get(MarkedSampleEntity_.nbkBatchNumber).cast(String.class), request.getNbkBatchNumber());
                conditions.textSearch(compound.get(CompoundEntity_.casNumber), request.getCasNumber());
                conditions.textSearch(root.get(MarkedSampleEntity_.sampleKey), request.getSampleKey());
                conditions.textSearch(compound.get(CompoundEntity_.formula).cast(String.class), request.getMolecularFormula(), MolFormula::normalize);
                conditions.numericSearch(compound.get(CompoundEntity_.molWeight), request.getMolWeight());
                conditions.textSearch(compound.get(CompoundEntity_.chemicalName), request.getChemicalName());
                conditions.textSearch(root.get(MarkedSampleEntity_.batchComment), request.getBatchComment());

                conditions.dictionary(root.get(MarkedSampleEntity_.compoundState), request.getCompoundState());
                if (request.getHealthHazards() != null) {
                    conditions.arrayContains(request.getHealthHazards().getId(), root.get(MarkedSampleEntity_.healthHazards));
                }
            });
        }};

        Paging paging = new Paging(pageNo, pageSize);
        Page<MarkedSampleEntity> page = doFindWithTotals(criteria, paging, null);
        return map(page, sampleMapper::markedSampleToDTO);
    }

    @Nullable
    public MarkedSampleEntity findByKey(UserEntity user, SampleSource source, String sampleKey) {
        return em.createQuery("from MarkedSample where user=?1 and source=?2 and sampleKey=?3", MarkedSampleEntity.class)
                .setParameter(1, user)
                .setParameter(2, source)
                .setParameter(3, sampleKey)
                .getSingleResultOrNull();
    }

    public Set<String> findMarkedKeys(UserEntity user, SampleSource source, Collection<String> sampleKeys) {
        if (sampleKeys.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(em.createQuery("select sampleKey from MarkedSample where user=?1 and source=?2 and sampleKey in ?3", String.class)
                .setParameter(1, user)
                .setParameter(2, source)
                .setParameter(3, sampleKeys)
                .getResultList());
    }
}
