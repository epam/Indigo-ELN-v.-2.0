package com.epam.indigoeln.core.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SortedPageUtil<T> {

    private final Function<String, Comparator<T>> ascComparator;
    private final Function<String, Comparator<T>> descComparator;

    public SortedPageUtil(Map<String, Function<T, String>> functionMap) {

        ascComparator = field -> (entity1, entity2) ->
        {
            String user1Field = functionMap.getOrDefault(field, entity -> "").apply(entity1);
            return user1Field == null ? 1 :
                    user1Field.compareToIgnoreCase(functionMap.getOrDefault(field, user -> "").apply(entity2));
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
            Comparator<T> userComparator = order.isAscending() ?
                    ascComparator.apply(property) :
                    descComparator.apply(property);
            comparator = (comparator == null) ? userComparator : comparator.thenComparing(userComparator);
        }
        return comparator;
    }
}
