package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.NumericSearch;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.TextSearch;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.BaseRepository;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.query.NativeQuery;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@ApplicationScoped
public class SampleRepository extends BaseRepository<SampleEntity> {

    @Inject
    SampleMapper sampleMapper;
    @Inject
    DictionaryItemRepository dictionaryItemRepository;
    @Inject
    DictionaryService dictionaryService;

    public SampleRepository() {
        super(EntityType.SAMPLE);
    }

    @Nullable
    public SampleEntity findDefaultSample(UUID compoundId) {
        Conditions conditions = new Conditions()
                .add("compound.id=?", compoundId)
                .add("nbkBatchNumber is null");
        return find(conditions.getQuery(), conditions.getValues()).firstResult();
    }

    public Page<SampleDTO> find(FindSamplesRequest request, Paging paging) {
        Conditions conditions = new Conditions()
                .addIfNotNull("full_text_search(searchVector, websearch_to_tsquery('english', ?))", request.getQuickSearch());
        if (request.getStructure() != null) {
            switch (request.getStructure().type()) {
                case EXACT -> {
                    conditions.add("bingo_exact_match(compound.molFile, ?, '')", request.getStructure().query());
                }
                case SUBSTRUCTURE -> {
                    conditions.add("bingo_substructure_match(compound.molFile, ?, '')", request.getStructure().query());
                }
                case SIMILARITY -> {
                    conditions.add("bingo_similarity_search(compound.molFile, 0.8, null, ?, 'Tanimoto')", request.getStructure().query());
                }
            }
        }
        addTextSearch(conditions, request.getNbkBatchNumber(), "nbkBatchNumber");;
        addTextSearch(conditions, request.getStrCode(), "strCode");
        addTextSearch(conditions, request.getMolecularFormula(), "compound.formula");
        addNumericSearch(conditions, request.getMolWeight(), "compound.molWeight");
        addTextSearch(conditions, request.getChemicalName(), "compound.chemicalName");
        addTextSearch(conditions, request.getCasNumber(), "compound.casNumber");
        addTextSearch(conditions, request.getExternalNumber(), "externalNumber");
        addTextSearch(conditions, request.getBatchComment(), "batchComment");
        if (request.getCompoundState() != null) {
            DictionaryItemEntity compoundState = dictionaryService.lookup(BuiltInDictionary.COMPONENT_STATE.name(), request.getCompoundState());
            conditions.add("compoundState = ?", compoundState);
        }
        if (request.getHealthHazards() != null) {
            DictionaryItemEntity healthHazard = dictionaryService.lookup(BuiltInDictionary.HEALTH_HAZARD.name(), request.getHealthHazards());
            conditions.add("? member of healthHazards", healthHazard);
        }
        if (request.getMarked() == Boolean.TRUE) {
            conditions.add("marked");
        } else if (request.getMarked() == Boolean.FALSE) {
            conditions.add("marked is null");
        }
        return doFindWithTotals(
                conditions,
                paging,
                Sort.by("compound.formula", "compound.saltCode.id", "compound.saltEQ100", "createdBy"),
                em.getEntityGraph("Sample.find"),
                sampleMapper::sampleToDTO
        );
    }

    private void addTextSearch(Conditions conditions, @Nullable TextSearch search, String field) {
        switch (search) {
            case TextSearch.BetweenSearch b -> conditions
                    .add("lower(" + field + ") >= ?", b.from().toLowerCase())
                    .add("lower(" + field + ") <= ?", b.to().toLowerCase());
            case TextSearch.ContainsSearch c -> conditions
                    .add("ilike(" + field + ", ?)", '%' + c.value() + '%');
            case TextSearch.EndsWithSearch e -> conditions
                    .add("ilike(" + field + ", ?)", '%' + e.value());
            case TextSearch.ExactSearch e -> conditions
                    .add("lower(" + field + ") = ?", e.value().toLowerCase());
            case TextSearch.StartsWithSearch s -> conditions
                    .add("ilike(" + field + ", ?)", s.value() + '%');
            case null -> {}
        }
    }

    private void addNumericSearch(Conditions conditions, @Nullable NumericSearch search, String field) {
        switch (search) {
            case NumericSearch.Equals e -> conditions
                    .add("floor(" + field + ") = ?", Math.floor(e.value()));
            case NumericSearch.GreaterThanOrEqual ge -> conditions
                    .add(field + " >= ?", ge.value());
            case NumericSearch.LessThanOrEqual le -> conditions
                    .add(field + " <= ?", le.value());
            case null -> {}
        }
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
