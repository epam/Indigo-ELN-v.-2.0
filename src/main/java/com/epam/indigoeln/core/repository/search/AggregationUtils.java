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
                .map(c -> createCriterion(c, prefix))
                .collect(toList());
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion) {
        return createCriterion(searchCriterion, "");
    }

    public static Criteria createCriterion(SearchCriterion criterion, String prefix) { //NOSONAR This method should check all the operators
        final Criteria criteria = Criteria.where(prefix + criterion.getField());
        switch (criterion.getCondition()) {
            case "contains":
                criteria.regex(".*" + criterion.getValue() + ".*", "i");
                break;
            case "starts with":
                criteria.regex(criterion.getValue() + ".*", "i");
                break;
            case "ends with":
                criteria.regex(".*" + criterion.getValue(), "i");
                break;
            case "in":
                criteria.in(convertToCollection(criterion.getValue()));
                break;
            case "=":
                criteria.is(convertToDouble(criterion.getValue()));
                break;
            case ">":
                criteria.gt(convertToDouble(criterion.getValue()));
                break;
            case ">=":
                criteria.gte(convertToDouble(criterion.getValue()));
                break;
            case "<":
                criteria.lt(convertToDouble(criterion.getValue()));
                break;
            case "<=":
                criteria.lte(convertToDouble(criterion.getValue()));
                break;
            default:
                criteria.is(criterion.getValue());
        }
        return criteria;
    }

    public static Optional<Criteria> andCriteria(Optional<Criteria>... mas) {
        List<Criteria> searchCriteria = Arrays.stream(mas)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        return andCriteria(searchCriteria);
    }

    public static Optional<Criteria> andCriteria(Collection<Criteria> criteriaCollection){
        if (!criteriaCollection.isEmpty()) {
            return Optional.of(new Criteria().andOperator(criteriaCollection.toArray(new Criteria[criteriaCollection.size()])));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Criteria> orCriteria(Collection<Criteria> criteriaCollection){
        if (!criteriaCollection.isEmpty()) {
            return Optional.of(new Criteria().orOperator(criteriaCollection.toArray(new Criteria[criteriaCollection.size()])));
        } else {
            return Optional.empty();
        }
    }

    public static Collection<?> convertToCollection(Object obj) {
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
