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
package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.service.exception.UriProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SortedPageUtil<T> {

    private final Function<String, Comparator<T>> ascComparator;
    private final Function<String, Comparator<T>> descComparator;

    public SortedPageUtil(Map<String, Function<T, String>> functionMap) {

        ascComparator = field -> (entity1, entity2) -> {
            Function<T, String> getValue = Optional.ofNullable(functionMap.get(field)).orElseThrow(() ->
                    UriProcessingException.cantParseSortingField(field));
            return String.CASE_INSENSITIVE_ORDER.compare(
                    Optional.ofNullable(getValue.apply(entity1)).orElse(""),
                    Optional.ofNullable(getValue.apply(entity2)).orElse(""));
        };
        descComparator = field -> this.ascComparator.apply(field).reversed();
    }

    public Page<T> getPage(List<T> entities, Pageable pageable) {
        if (pageable.getSort() != null) {
            Comparator<T> comparator = pageableToComparator(pageable);
            entities.sort(comparator);
        }

        int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
        if (fromIndex > entities.size()) {
            fromIndex = entities.size();
        }

        int toIndex = fromIndex + pageable.getPageSize();
        if (toIndex > entities.size()) {
            toIndex = entities.size();
        }

        return new PageImpl<>(entities.subList(fromIndex, toIndex), pageable, entities.size());
    }

    private Comparator<T> pageableToComparator(Pageable pageable) {
        Comparator<T> comparator = null;
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            Comparator<T> userComparator = order.isAscending()
                    ? ascComparator.apply(property)
                    : descComparator.apply(property);
            comparator = (comparator == null)
                    ? userComparator
                    : comparator.thenComparing(userComparator);
        }
        return comparator;
    }
}
