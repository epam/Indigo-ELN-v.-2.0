package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.compound.model.search.NumericSearch;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserEntity_;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Dependent
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CriteriaConditions {

    private static final String SIMILARITY_METRIC_TANIMOTO = "Tanimoto";

    @Inject
    HibernateCriteriaBuilder cb;
    @Inject
    DictionaryService dictionaryService;

    private final List<Predicate> predicates = new ArrayList<>();

    public void add(@Nullable Predicate predicate) {
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    public void add(@Nullable Expression<Boolean> expression) {
        if (expression != null) {
            predicates.add(cb.isTrue(expression));
        }
    }

    public void textSearch(Expression<String> attribute, @Nullable TextSearch search) {
        textSearch(attribute, search, Function.identity());
    }

    public void textSearch(Expression<String> attribute, @Nullable TextSearch search, Function<String, String> valueConverter) {
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
            predicates.add(predicate);
        }
    }

    public void numericSearch(Expression<Double> attribute, @Nullable NumericSearch search) {
        Predicate predicate = switch (search) {
            case null -> null;
            case NumericSearch.Equals e -> cb.floor(attribute).equalTo(Math.floor(e.value()));
            case NumericSearch.GreaterThanOrEqual ge -> cb.greaterThanOrEqualTo(attribute, ge.value());
            case NumericSearch.LessThanOrEqual le -> cb.lessThanOrEqualTo(attribute, le.value());
        };
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    public void dictionary(Expression<DictionaryItemEntity> attribute, @Nullable DictionaryItemRef search) {
        if (search != null) {
            DictionaryItemEntity item = dictionaryService.lookup(search);
            predicates.add(attribute.equalTo(item));
        }
    }

    public void multiDictionary(Expression<? extends Collection<DictionaryItemEntity>> attribute, @Nullable DictionaryItemRef search) {
        if (search != null) {
            DictionaryItemEntity item = dictionaryService.lookup(search);
            predicates.add(cb.isMember(item, attribute));
        }
    }

    public void fullTextSearch(Expression<SearchVector> attribute, @Nullable String search, @Nullable Expression<String> name) {
        if (search != null) {
            Predicate predicate = cb.isTrue(cb.function("full_text_search", Boolean.class, attribute, cb.literal("english"), cb.literal(search)));
            if (name != null) {
                predicate = cb.or(predicate, cb.ilike(name, cb.literal("%" + search + "%")));
            }
            predicates.add(predicate);
        }
    }

    public Expression<Double> fullTextRank(Expression<SearchVector> attribute, String search) {
        return cb.function("ts_rank", Double.class, attribute, cb.literal(search));
    }

    public Expression<String> fullTextHeadline(Expression<String> attribute, String search, String options) {
        return cb.function("ts_headline", String.class, cb.literal("english"), attribute, cb.literal("english"), cb.literal(search), cb.literal(options));
    }

    public void moleculeSearch(Expression<String> attribute, @Nullable StructuralSearch search) {
        if (search != null) {
            Expression<Boolean> expression = switch (search.type()) {
                case EXACT -> cb.function("bingo_exact_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SUBSTRUCTURE -> cb.function("bingo_substructure_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SIMILARITY -> cb.function("bingo_similarity_match", Boolean.class, attribute, cb.literal(0.8), cb.nullLiteral(Double.class), cb.literal(search.query()), cb.literal("Tanimoto"));
            };
            predicates.add(cb.isTrue(expression));
        }
    }

    public Expression<Double> moleculeSimilarity(Expression<String> attribute, String query) {
        return cb.function("bingo_getsimilarity", Double.class, attribute, cb.literal(query), cb.literal(SIMILARITY_METRIC_TANIMOTO));
    }

    public void reactionSearch(Expression<String> attribute, @Nullable StructuralSearch search) {
        if (search != null) {
            Expression<Boolean> expression = switch (search.type()) {
                case EXACT -> cb.function("bingo_rexact_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SUBSTRUCTURE -> cb.function("bingo_rsubstructure_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SIMILARITY -> {
                    throw new InvalidRequestException("Reaction similarity search is not supported");
                }
            };
            predicates.add(cb.isTrue(expression));
        }
    }

    public void bool(Expression<Boolean> attribute, @Nullable Boolean search) {
        if (search != null) {
            predicates.add(search ? cb.isTrue(attribute) : cb.isFalse(attribute));
        }
    }

    public void user(Path<UserEntity> attribute, @Nullable Collection<UserRef> search) {
        if (search != null) {
            predicates.add(cb.in(attribute.get(UserEntity_.username), search.stream().map(UserRef::getUsername).toList()));
        }
    }

    @ApplicationScoped
    public static class Factory {

        @Inject
        Instance<CriteriaConditions> instance;

        public void withConditions(Consumer<List<Predicate>> applier, Consumer<CriteriaConditions> block) {
            CriteriaConditions conditions = instance.get();
            try {
                block.accept(conditions);
                applier.accept(conditions.predicates);
            } finally {
                instance.destroy(conditions);
            }
        }
    }
}
