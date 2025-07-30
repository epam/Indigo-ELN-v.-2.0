package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.repository.BaseRepository;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SampleRepository extends BaseRepository<SampleEntity> {

    @Inject
    SampleMapper sampleMapper;

    public SampleRepository() {
        super(EntityType.SAMPLE);
    }

    @Nullable
    public SampleEntity findDefaultSample(UUID compoundId) {
        Conditions conditions = new Conditions()
                .add("compound.id=?", compoundId)
                .add("batchNumber is null");
        return find(conditions.getQuery(), conditions.getValues()).firstResult();
    }

    public List<SampleEntity> find(FindSamplesRequest request) {
        Conditions conditions = new Conditions();
        if (request.getStructure() != null) {
            InvalidRequestException.validate(request.getStructureSearchType() != null, "structureSearchType is required when structure is provided");
        }
        switch (request.getStructureSearchType()) {
            case EXACT -> {
                conditions.add("bingo_exact_match(compound.molFile, ?, '')", request.getStructure());
            }
            case SUBSTRUCTURE -> {
                conditions.add("bingo_substructure_match(compound.molFile, ?, '')", request.getStructure());
            }
            case SIMILARITY -> {
                conditions.add("bingo_similarity_search(compound.molFile, 0.8, null, ?, 'Tanimoto')", request.getStructure());
            }
        }
        return find(conditions.getQuery(), conditions.getValues()).list();
    }

    @Nullable
    public String getLastSampleStrCode(String compoundStrCode) {
        return em.createQuery("select strCode from Sample where strCode like ?1 order by strCode desc", String.class)
                .setParameter(1, compoundStrCode + '%')
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
