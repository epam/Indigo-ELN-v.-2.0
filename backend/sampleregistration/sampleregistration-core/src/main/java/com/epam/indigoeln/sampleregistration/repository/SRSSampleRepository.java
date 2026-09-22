package com.epam.indigoeln.sampleregistration.repository;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.sampleregistration.entity.SRSCompoundEntity;
import com.epam.indigoeln.sampleregistration.entity.SRSCompoundEntity_;
import com.epam.indigoeln.sampleregistration.entity.SRSSampleEntity;
import com.epam.indigoeln.sampleregistration.entity.SRSSampleEntity_;
import com.epam.indigoeln.sampleregistration.mapper.SRSSampleMapper;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.STRCodeSample;
import com.epam.indigoeln.sampleregistration.util.SRSCriteriaConditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.SynchronizeableQuery;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

@ApplicationScoped
public class SRSSampleRepository extends BaseRepository<SRSSampleEntity> {

    @Inject
    SRSSampleMapper sampleMapper;
    @Inject
    SRSCriteriaConditions.Factory criteriaConditionsFactory;
    @Inject
    HibernateCriteriaBuilder cb;

    public SRSSampleRepository() {
        super(SRSSampleEntity.class);
    }

    @Nullable
    public STRCodeSample getLastSampleStrCode(String compoundStrCode) {
        //noinspection unchecked
        NativeQuery<String> query = (NativeQuery<String>) em.createNativeQuery("select str_code from SRS_Sample where str_code like ?1 order by str_code desc", String.class);
        query.unwrap(SynchronizeableQuery.class).addSynchronizedEntityClass(SRSSampleEntity.class);
        return query
                .setParameter(1, compoundStrCode + '%')
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(STRCodeSample::parse)
                .orElse(null);
    }

    public Page<SRSSampleDTO> find(SRSFindSamplesRequest request, int pageNo, int pageSize) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<SRSSampleEntity> root = from(SRSSampleEntity.class);
            JpaJoin<SRSSampleEntity, SRSCompoundEntity> compound = root.join(SRSSampleEntity_.compound); // will be optimized away if not used
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                orderBy(asc(root.get(SRSSampleEntity_.id))); // default sort, can be overridden
                conditions.fullTextSearch(root.get(SRSSampleEntity_.searchVector), request.getQuickSearch(), null);

                conditions.moleculeSearch(compound.get(SRSCompoundEntity_.molFile), request.getStructure());
                if (request.getStructure() != null && request.getStructure().type() != StructuralSearch.Type.EXACT) {
                    orderBy(desc(conditions.moleculeSimilarity(compound.get(SRSCompoundEntity_.molFile), request.getStructure().query())));
                }

                conditions.textSearch(root.get(SRSSampleEntity_.strCode).cast(String.class), request.getStrCodeSample());
                conditions.textSearch(compound.get(SRSCompoundEntity_.formula).cast(String.class), request.getMolecularFormula(), MolFormula::normalize);
                conditions.numericSearch(compound.get(SRSCompoundEntity_.molWeight), request.getMolWeight());
                conditions.textSearch(compound.get(SRSCompoundEntity_.chemicalName), request.getChemicalName());
                conditions.textSearch(root.get(SRSSampleEntity_.batchComment), request.getBatchComment());

                if (request.getCompoundState() != null) {
                    conditions.add(equal(root.get(SRSSampleEntity_.compoundState), request.getCompoundState()));
                }
                // !!!
//                if (request.getHealthHazards() != null) {
//                    conditions.add(cb.isMember(request.getHealthHazards(), root.get(SRSSampleEntity_.healthHazards)));
//                }
            });
        }};

        Paging paging = new Paging(pageNo, pageSize);
        Page<SRSSampleEntity> page = doFindWithTotals(criteria, paging, null);
        return ModelUtil.map(page, sampleMapper::sampleToDTO);
    }
}
