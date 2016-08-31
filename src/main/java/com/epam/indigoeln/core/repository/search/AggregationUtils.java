package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;

public final class AggregationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationUtils.class);

    public static void fillCriteria(Criteria criteria, SearchCriterion searchCriterion) {
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
