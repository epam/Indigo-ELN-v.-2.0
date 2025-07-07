package com.epam.indigoeln.compound.repository;

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

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SampleRepository extends BaseRepository<SampleEntity> {

    @Inject
    SampleMapper sampleMapper;

    public SampleRepository() {
        super(EntityType.SAMPLE);
    }

    public SampleEntity findDefaultSample(UUID compoundId) {
        Conditions conditions = new Conditions()
                .add("compound.id=?", compoundId)
                .add("batchNumber is null");
        return find(conditions.getQuery(), conditions.getValues()).singleResult();
    }

    public List<SampleEntity> find(FindSamplesRequest request) {
        Conditions conditions = new Conditions();
        if (request.getStructureSearchType() != null) {
            conditions.add("bingo_substructure_match(compound.molFile, ?, '')", request.getStructure());
        }
        return find(conditions.getQuery(), conditions.getValues()).list();
    }

    public SampleEntity getLastSampleByStrCode(CompoundEntity compound) {
        return find("compound=?1 and strCode is not null", Sort.descending("strCode"), compound)
                .firstResult();
    }
}
