package com.epam.indigoeln.eln.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class Page<T> {

    int pageNo;
    int pageSize;
    long totalItems;
    int totalPages;
    List<T> items;

    public static <T> Page<T> of(Paging paging, long totalItems, List<T> items) {
        int totalPages = (int) Math.ceil((double) totalItems / paging.getPageSizeOrDefault());
        return new Page<>(paging.getPageNoOrDefault(), paging.getPageSizeOrDefault(), totalItems, totalPages, items);
    }
}
