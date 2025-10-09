package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.eln.repository.BaseRepository;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.query.NativeQuery;
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
        Conditions conditions = new Conditions()
                .addIfNotNull("full_text_search(searchVector, websearch_to_tsquery('english', ?))", request.getQuickSearch());
        if (request.getStructureSearchType() != null) {
            InvalidRequestException.validate(request.getStructure() != null, "structureSearchType is required when structure is provided");
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
        } else {
            InvalidRequestException.validate(request.getStructure() == null, "structure cannot be used without structureSearchType");
        }
        return find(conditions.getQuery(), conditions.getValues()).list();
    }

    @Nullable
    public STRCodeSample getLastSampleStrCode(String compoundStrCode) {
        //noinspection unchecked
        NativeQuery<String> query = (NativeQuery<String>) em.createNativeQuery("select str_code from Sample where str_code like ?1 order by str_code desc", String.class);
        return query
                .setParameter(1, compoundStrCode + '%')
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(STRCodeSample::parse)
                .orElse(null);
    }
}
