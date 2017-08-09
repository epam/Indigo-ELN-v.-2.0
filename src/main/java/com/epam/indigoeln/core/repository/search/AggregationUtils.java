package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("rawtypes")
public final class AggregationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationUtils.class);

    private AggregationUtils() {
    }

    public static List<Criteria> createCriteria(Collection<SearchCriterion> criteria) {
        return createCriteria(criteria, "");
    }

    public static List<Criteria> createCriteria(Collection<SearchCriterion> criteria, String prefix) {
        return criteria.stream()
                .map(c -> createCriterion(c.getCondition(), prefix + c.getField(), c.getValue()))
                .collect(toList());
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion) {
        return createCriterion(searchCriterion.getCondition(), searchCriterion.getField(), searchCriterion.getValue());
    }

    public static Criteria createCriterion(String condition, String key, Object value) {
        switch (condition) {
            case "contains":
                return Criteria.where(key).regex(regexContains(value), "i");
            case "starts with":
                return Criteria.where(key).regex(regexStartsWith(value), "i");
            case "ends with":
                return Criteria.where(key).regex(regexEndsWith(value), "i");
            case "in":
                return Criteria.where(key).in(convertToCollection(value));
            case "=":
                return Criteria.where(key).is(convertToDouble(value));
            case ">":
                return Criteria.where(key).gt(convertToDouble(value));
            case ">=":
                return Criteria.where(key).gte(convertToDouble(value));
            case "<":
                return Criteria.where(key).lt(convertToDouble(value));
            case "<=":
                return Criteria.where(key).lte(convertToDouble(value));
            default:
                return Criteria.where(key).is(value);
        }
    }

    public static Optional<Criteria> andCriteria(Optional<Criteria>... mas) {
        List<Criteria> searchCriteria = Arrays.stream(mas)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        return andCriteria(searchCriteria);
    }

    public static Optional<Criteria> andCriteria(Collection<Criteria> criteriaCollection) {
        if (!criteriaCollection.isEmpty()) {
            return Optional.of(new Criteria().andOperator(criteriaCollection.toArray(new Criteria[criteriaCollection.size()])));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Criteria> orCriteria(Collection<Criteria> criteriaCollection) {
        if (!criteriaCollection.isEmpty()) {
            return Optional.of(new Criteria().orOperator(criteriaCollection.toArray(new Criteria[criteriaCollection.size()])));
        } else {
            return Optional.empty();
        }
    }

    public static Collection convertToCollection(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof Collection) {
            return (Collection) obj;
        }
        return Arrays.asList(obj.toString().split(";"));
    }

    private static Object convertToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        final Double result = getDouble(obj.toString());
        return result == null ? obj : result;
    }

    private static Double getDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            LOGGER.warn("Unable to convert value " + str + " to double.");
        }
        return null;
    }

    private static String regexContains(Object value) {
        return ".*" + value + ".*";
    }

    private static String regexStartsWith(Object value) {
        return value + ".*";
    }

    private static String regexEndsWith(Object value) {
        return ".*" + value;
    }
}
