package com.epam.indigoeln.compound.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.CompoundEntity_;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.entity.SampleEntity_;
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.SynchronizeableQuery;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.map;

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
        return doFindOne(conditions);
    }

    public Page<SampleDTO> find(FindSamplesRequest request, @Nullable Boolean marked, int pageNo, int pageSize) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<SampleEntity> root = from(SampleEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            where(buildConditions(request, marked, root, getCriteriaBuilder()));
            orderBy(asc(root.get(SampleEntity_.id)));
        }};

        Paging paging = new Paging(pageNo, pageSize);
        Page<SampleEntity> page = doFindWithTotals(criteria, paging, em.getEntityGraph("Sample.find"));
        return map(page, sampleMapper::sampleToDTO);
    }

    private List<Predicate> buildConditions(FindSamplesRequest request, @Nullable Boolean marked, JpaRoot<SampleEntity> root, HibernateCriteriaBuilder cb) {
        List<Predicate> conditions = new ArrayList<>();
        if (request.getQuickSearch() != null) {
            conditions.add(cb.isTrue(cb.function("full_text_search", Boolean.class, root.get(SampleEntity_.searchVector), cb.literal("english"), cb.literal(request.getQuickSearch()))));
        }
        addTextSearch(root.get(SampleEntity_.nbkBatchNumber).cast(String.class), request.getNbkBatchNumber(), conditions, cb);
        addTextSearch(root.get(SampleEntity_.externalNumber), request.getExternalNumber(), conditions, cb);
        addTextSearch(root.get(SampleEntity_.batchComment), request.getBatchComment(), conditions, cb);
        if (request.getCompoundState() != null) {
            DictionaryItemEntity compoundState = dictionaryService.lookup(request.getCompoundState());
            conditions.add(root.get(SampleEntity_.compoundState).equalTo(compoundState));
        }
        if (request.getHealthHazards() != null) {
            DictionaryItemEntity healthHazard = dictionaryService.lookup(request.getHealthHazards());
            conditions.add(cb.isMember(healthHazard, root.get(SampleEntity_.healthHazards)));
        }
        if (marked != null) {
            conditions.add(marked
                    ? cb.isTrue(root.get(SampleEntity_.marked))
                    : cb.isFalse(root.get(SampleEntity_.marked))
            );
        }
        if (request.getStructure() != null || request.getCompoundKey() != null || request.getMolecularFormula() != null || request.getMolWeight() != null || request.getChemicalName() != null || request.getCasNumber() != null) {
            JpaJoin<SampleEntity, CompoundEntity> compound = root.join(SampleEntity_.compound);
            if (request.getStructure() != null) {
                switch (request.getStructure().type()) {
                    case EXACT -> {
                        conditions.add(cb.isTrue(cb.function("bingo_exact_match", Boolean.class, compound.get(CompoundEntity_.molFile), cb.literal(request.getStructure().query()), cb.literal(""))));
                    }
                    case SUBSTRUCTURE -> {
                        conditions.add(cb.isTrue(cb.function("bingo_substructure_match", Boolean.class, compound.get(CompoundEntity_.molFile), cb.literal(request.getStructure().query()), cb.literal(""))));
                    }
                    case SIMILARITY -> {
                        conditions.add(cb.isTrue(cb.function("bingo_similarity_match", Boolean.class, compound.get(CompoundEntity_.molFile), cb.literal(0.8), cb.nullLiteral(Double.class), cb.literal(request.getStructure().query()), cb.literal("Tanimoto"))));
                    }
                }
            }
            addTextSearch(compound.get(CompoundEntity_.compoundKey), request.getCompoundKey(), conditions, cb);
            addTextSearch(compound.get(CompoundEntity_.formula).cast(String.class), request.getMolecularFormula(), MolFormula::normalize, conditions, cb);
            addNumericSearch(compound.get(CompoundEntity_.molWeight), request.getMolWeight(), conditions, cb);
            addTextSearch(compound.get(CompoundEntity_.chemicalName), request.getChemicalName(), conditions, cb);
            addTextSearch(compound.get(CompoundEntity_.casNumber), request.getCasNumber(), conditions, cb);
        }
        return conditions;
    }

    private void addTextSearch(Expression<String> attribute, @Nullable TextSearch search, List<Predicate> target, HibernateCriteriaBuilder cb) {
        addTextSearch(attribute, search, Function.identity(), target, cb);
    }

    private void addTextSearch(Expression<String> attribute, @Nullable TextSearch search, Function<String, String> valueConverter, List<Predicate> target, HibernateCriteriaBuilder cb) {
        Predicate predicate = switch (search) {
            case null -> null;
            case TextSearch.WithValue w -> {
                String value = valueConverter.apply(w.value().toLowerCase());
                yield switch (w) {
                    case TextSearch.ExactSearch e -> cb.lower(attribute).equalTo(value);
                    case TextSearch.StartsWithSearch s -> cb.ilike(attribute, value + '%');
                    case TextSearch.EndsWithSearch e -> cb.ilike(attribute, '%' + value);
                    case TextSearch.ContainsSearch c -> cb.ilike(attribute, '%' + value + '%');
                };
            }
            case TextSearch.BetweenSearch b -> {
                yield cb.between(cb.lower(attribute), valueConverter.apply(b.from().toLowerCase()), valueConverter.apply(b.to().toLowerCase()));
            }
        };
        if (predicate != null) {
            target.add(predicate);
        }
    }

    private void addNumericSearch(Expression<Double> attribute, @Nullable NumericSearch search, List<Predicate> target, HibernateCriteriaBuilder cb) {
        Predicate predicate = switch (search) {
            case null -> null;
            case NumericSearch.Equals e -> cb.floor(attribute).equalTo(Math.floor(e.value()));
            case NumericSearch.GreaterThanOrEqual ge -> cb.greaterThanOrEqualTo(attribute, ge.value());
            case NumericSearch.LessThanOrEqual le -> cb.lessThanOrEqualTo(attribute, le.value());
        };
        if (predicate != null) {
            target.add(predicate);
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
