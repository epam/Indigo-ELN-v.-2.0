package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                .map(c -> createCriterion(c, prefix))
                .collect(toList());
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion) {
        return createCriterion(searchCriterion, "");
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion, String prefix) {
        return createCriterion(searchCriterion.getField(), searchCriterion.getValue(), searchCriterion.getCondition(), prefix);
    }

    public static Criteria createCriterion(String field, Object value, String condition, String prefix) {
        final Criteria criteria = Criteria.where(prefix + field);
        switch (condition) {
            case "contains":
                criteria.regex(".*" + value + ".*","i");
                break;
            case "starts with":
                criteria.regex(value + ".*","i");
                break;
            case "ends with":
                criteria.regex(".*" + value,"i");
                break;
            case "in":
                criteria.in(convertToCollection(value));
                break;
            case "=":
                criteria.is(convertToDouble(value));
                break;
            case ">":
                criteria.gt(convertToDouble(value));
                break;
            case ">=":
                criteria.gte(convertToDouble(value));
                break;
            case "<":
                criteria.lt(convertToDouble(value));
                break;
            case "<=":
                criteria.lte(convertToDouble(value));
                break;
            default:
                criteria.is(value);
        }
        return criteria;
    }

    private static Collection<?> convertToCollection(Object obj) {
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

}
