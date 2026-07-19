package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.compound.model.search.NumericSearch;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Dependent
public class CriteriaConditions {

    private final HibernateCriteriaBuilder cb;

    @Inject
    DictionaryService dictionaryService;

    private final List<Predicate> predicates = new ArrayList<>();

    @Inject
    CriteriaConditions(EntityManager em) {
        cb = (HibernateCriteriaBuilder) em.getCriteriaBuilder();
    }

    public void apply(Consumer<List<Predicate>> consumer) {
        consumer.accept(predicates);
    }

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
        jakarta.persistence.criteria.Predicate predicate = switch (search) {
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

    public void fullTextSearch(Expression<String> attribute, @Nullable String search, Function<String, @Nullable List<Predicate>> alternativesFn) {
        if (search != null) {
            Predicate predicate = cb.isTrue(cb.function("full_text_search", Boolean.class, attribute, cb.literal("english"), cb.literal(search)));
            List<Predicate> alternatives = alternativesFn.apply(search);
            if (alternatives != null) {
                predicate = cb.or(Stream.concat(Stream.of(predicate), alternatives.stream()).toList());
            }
            predicates.add(predicate);
        }
    }
    
    public void structureSearch(Expression<String> attribute, @Nullable StructuralSearch search) {
        if (search != null) {
            Expression<Boolean> expression = switch (search.type()) {
                case EXACT -> cb.function("bingo_exact_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SUBSTRUCTURE -> cb.function("bingo_substructure_match", Boolean.class, attribute, cb.literal(search.query()), cb.literal(""));
                case SIMILARITY -> cb.function("bingo_similarity_match", Boolean.class, attribute, cb.literal(0.8), cb.nullLiteral(Double.class), cb.literal(search.query()), cb.literal("Tanimoto"));
            };
            predicates.add(cb.isTrue(expression));
        }
    }

    public void bool(Expression<Boolean> attribute, @Nullable Boolean search) {
        if (search != null) {
            predicates.add(search ? cb.isTrue(attribute) : cb.isFalse(attribute));
        }
    }
}
