/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    private static List<Criteria> createCriteria(Collection<SearchCriterion> criteria, String prefix) {
        return criteria.stream()
                .map(c -> createCriterion(c, prefix))
                .collect(toList());
    }

    public static Criteria createCriterion(SearchCriterion searchCriterion) {
        return createCriterion(searchCriterion, "");
    }

    static Criteria createCriterion(SearchCriterion searchCriterion, String prefix) {
        return createCriterion(searchCriterion.getCondition(), prefix + searchCriterion.getField(),
                searchCriterion.getValue());
    }

    private static Criteria createCriterion(String condition, String key, Object value) {
        Criteria regex = createCriterionRegex(condition, key, value);

        if (regex != null) {
            return regex;
        }

        switch (condition) {
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

    private static Criteria createCriterionRegex(String condition, String key, Object value) {
        switch (condition) {
            case "contains":
                return Criteria.where(key).regex(regexContains(value), "i");
            case "starts with":
                return Criteria.where(key).regex(regexStartsWith(value), "i");
            case "ends with":
                return Criteria.where(key).regex(regexEndsWith(value), "i");
            default:
                return null;
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
            return Optional.of(new Criteria().andOperator(criteriaCollection
                    .toArray(new Criteria[criteriaCollection.size()])));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Criteria> orCriteria(Collection<Criteria> criteriaCollection) {
        if (!criteriaCollection.isEmpty()) {
            return Optional.of(new Criteria().orOperator(criteriaCollection
                    .toArray(new Criteria[criteriaCollection.size()])));
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

    public static Object convertToDouble(Object obj) {
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
