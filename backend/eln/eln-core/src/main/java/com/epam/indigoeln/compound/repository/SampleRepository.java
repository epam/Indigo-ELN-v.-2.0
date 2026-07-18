package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.NumericSearch;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.reaction.model.MolFormula;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.SynchronizeableQuery;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class SampleRepository extends BaseRepository<SampleEntity> {

    @Inject
    SampleMapper sampleMapper;
    @Inject
    DictionaryItemRepository dictionaryItemRepository;
    @Inject
    DictionaryService dictionaryService;

    public SampleRepository() {
        super(ELNEntityType.SAMPLE, SampleEntity.class);
    }

    @Nullable
    public SampleEntity findDefaultSample(UUID compoundId) {
        Conditions conditions = new Conditions()
                .add("compound.id=?", compoundId)
                .add("nbkBatchNumber is null");
        return find(conditions.getQuery(), conditions.getValues()).firstResult();
    }

    public Pair<List<SampleDTO>, Long> find(FindSamplesRequest request, @Nullable Boolean marked, int limit, @Nullable UUID nextAfter) {
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
        addTextSearch(conditions, request.getCompoundKey(), "compound.strCode");
        addTextSearch(conditions, request.getMolecularFormula(), "compound.formula", MolFormula::normalize);
        addNumericSearch(conditions, request.getMolWeight(), "compound.molWeight");
        addTextSearch(conditions, request.getChemicalName(), "compound.chemicalName");
        addTextSearch(conditions, request.getCasNumber(), "compound.casNumber");
        addTextSearch(conditions, request.getExternalNumber(), "externalNumber");
        addTextSearch(conditions, request.getBatchComment(), "batchComment");
        if (request.getCompoundState() != null) {
            DictionaryItemEntity compoundState = dictionaryService.lookup(request.getCompoundState());
            conditions.add("compoundState = ?", compoundState);
        }
        if (request.getHealthHazards() != null) {
            DictionaryItemEntity healthHazard = dictionaryService.lookup(request.getHealthHazards());
            conditions.add("? member of healthHazards", healthHazard);
        }
        if (marked == Boolean.TRUE) {
            conditions.add("marked");
        } else if (marked == Boolean.FALSE) {
            conditions.add("marked is null");
        }

        Sort sort = Sort.by("id");
        PanacheQuery<SampleEntity> query = find(conditions.getQuery(), sort, conditions.getValues());
        long totalCount = query.count();

        if (nextAfter != null) {
            conditions.add("id > ?", nextAfter);
        }
        query = find(conditions.getQuery(), sort, conditions.getValues())
                .page(0, limit)
                .withHint("jakarta.persistence.loadgraph", em.getEntityGraph("Sample.find"));
        return Pair.of(query.stream().map(sampleMapper::sampleToDTO).toList(), totalCount);
    }

    private void addTextSearch(Conditions conditions, @Nullable TextSearch search, String field) {
        addTextSearch(conditions, search, field, Function.identity());
    }

    private void addTextSearch(Conditions conditions, @Nullable TextSearch search, String field, Function<String, String> valueConverter) {
        switch (search) {
            case null -> {}
            case TextSearch.ExactSearch e -> conditions
                    .add("lower(" + field + ") = ?", valueConverter.apply(e.value()).toLowerCase());
            case TextSearch.StartsWithSearch s -> conditions
                    .add("ilike(" + field + ", ?)", valueConverter.apply(s.value()) + '%');
            case TextSearch.ContainsSearch c -> conditions
                    .add("ilike(" + field + ", ?)", '%' + valueConverter.apply(c.value()) + '%');
            case TextSearch.EndsWithSearch e -> conditions
                    .add("ilike(" + field + ", ?)", '%' + valueConverter.apply(e.value()));
            case TextSearch.BetweenSearch b -> conditions
                    .add("lower(" + field + ") >= ?", valueConverter.apply(b.from()).toLowerCase())
                    .add("lower(" + field + ") <= ?", valueConverter.apply(b.to()).toLowerCase());
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
        query.unwrap(SynchronizeableQuery.class).addSynchronizedEntityClass(SampleEntity.class);
        return query
                .setParameter(1, compoundStrCode + '%')
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(STRCodeSample::parse)
                .orElse(null);
    }
}
