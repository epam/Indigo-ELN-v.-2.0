package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.CompoundEntity_;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.entity.SampleEntity_;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.util.ELNCriteriaConditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class SampleRepository extends BaseRepository<SampleEntity> {

    @Inject
    SampleMapper sampleMapper;
    @Inject
    ELNCriteriaConditions.Factory criteriaConditionsFactory;

    public SampleRepository() {
        super(SampleEntity.class);
    }

    @Nullable
    public SampleEntity findDefaultSample(UUID compoundId) {
        CriteriaDefinition<SampleEntity> criteria = new CriteriaDefinition<>(em, SampleEntity.class) {{
            JpaRoot<SampleEntity> root = from(SampleEntity.class);
            select(root);
            where(root.get(SampleEntity_.compound).get(CompoundEntity_.id).equalTo(compoundId),
                    root.get(SampleEntity_.nbkBatchNumber).isNull());
        }};
        return doFindOne(criteria, null);
    }

    public Page<SampleDTO> find(FindSamplesRequest request, @Nullable Boolean marked, int pageNo, int pageSize) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<SampleEntity> root = from(SampleEntity.class);
            JpaJoin<SampleEntity, CompoundEntity> compound = root.join(SampleEntity_.compound); // will be optimized away if not used
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                orderBy(asc(root.get(SampleEntity_.id))); // default sort, can be overridden
                conditions.fullTextSearch(root.get(SampleEntity_.searchVector), request.getQuickSearch(), null);

                conditions.moleculeSearch(compound.get(CompoundEntity_.molFile), request.getStructure());
                if (request.getStructure() != null && request.getStructure().type() != StructuralSearch.Type.EXACT) {
                    orderBy(desc(conditions.moleculeSimilarity(compound.get(CompoundEntity_.molFile), request.getStructure().query())));
                }

                conditions.textSearch(compound.get(CompoundEntity_.compoundKey), request.getCompoundKey());
                conditions.textSearch(root.get(SampleEntity_.nbkBatchNumber).cast(String.class), request.getNbkBatchNumber());
                conditions.textSearch(compound.get(CompoundEntity_.casNumber), request.getCasNumber());
                conditions.textSearch(root.get(SampleEntity_.sampleKey), request.getExternalNumber());
                conditions.textSearch(compound.get(CompoundEntity_.formula).cast(String.class), request.getMolecularFormula(), MolFormula::normalize);
                conditions.numericSearch(compound.get(CompoundEntity_.molWeight), request.getMolWeight());
                conditions.textSearch(compound.get(CompoundEntity_.chemicalName), request.getChemicalName());
                conditions.textSearch(root.get(SampleEntity_.batchComment), request.getBatchComment());

                conditions.dictionary(root.get(SampleEntity_.compoundState), request.getCompoundState());
                conditions.multiDictionary(root.get(SampleEntity_.healthHazards), request.getHealthHazards());

                conditions.bool(root.get(SampleEntity_.marked), marked);
            });
        }};

        Paging paging = new Paging(pageNo, pageSize);
        Page<SampleEntity> page = doFindWithTotals(criteria, paging, em.getEntityGraph("Sample.find"));
        return map(page, sampleMapper::sampleToDTO);
    }
}
