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

public final class AggregationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationUtils.class);

    public static Criteria createCriteria(Collection<SearchCriterion> criteria) {
        return createCriteria(criteria, "");
    }

    public static Criteria createCriteria(Collection<SearchCriterion> criteria, String prefix) {
        List<Criteria> stageCriteria = criteria.stream()
                .map(c -> createCriterion(c, prefix))
                .collect(toList());
        Criteria[] mongoCriteriaList = stageCriteria.toArray(new Criteria[stageCriteria.size()]);
        return new Criteria().andOperator(mongoCriteriaList);
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion) {
        return createCriterion(searchCriterion, "");
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion, String prefix) {
        final Criteria criteria = Criteria.where(prefix + searchCriterion.getField());
        Object value = searchCriterion.getValue();
        switch (searchCriterion.getCondition()) {
            case "contains":
                criteria.regex(".*" + value + ".*");
                break;
            case "starts with":
                criteria.regex(value + ".*");
                break;
            case "ends with":
                criteria.regex(".*" + value);
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

    private static Collection<String> convertToCollection(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
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