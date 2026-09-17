package com.epam.indigoeln.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Value
@Builder
@Jacksonized
public class Page<T> {

    int pageNo;
    int pageSize;
    /** Null when the total was not computed, e.g. on pages after the first of a global search. */
    @Nullable
    Long totalItems;
    @Nullable
    Integer totalPages;
    /** Whether a page after this one exists; the only end-of-results signal when the total is null. */
    boolean hasMore;
    List<T> items;

    @JsonIgnore
    public Paging getPaging() {
        return new Paging(pageNo, pageSize);
    }

    public static <T> Page<T> of(Paging paging, long totalItems, List<T> items) {
        int totalPages = totalPages(paging, totalItems);
        return new Page<>(paging.getPageNoOrDefault(), paging.getPageSizeOrDefault(), totalItems, totalPages, paging.getPageNoOrDefault() + 1 < totalPages, items);
    }

    public static <T> Page<T> of(Paging paging, @Nullable Long totalItems, List<T> items, boolean hasMore) {
        Integer totalPages = totalItems != null ? totalPages(paging, totalItems) : null;
        return new Page<>(paging.getPageNoOrDefault(), paging.getPageSizeOrDefault(), totalItems, totalPages, hasMore, items);
    }

    private static int totalPages(Paging paging, long totalItems) {
        return (int) Math.ceil((double) totalItems / paging.getPageSizeOrDefault());
    }
}
