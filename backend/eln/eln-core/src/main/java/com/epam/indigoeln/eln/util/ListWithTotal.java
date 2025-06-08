package com.epam.indigoeln.eln.util;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ListWithTotal<T>(
        @NotNull List<T> list,
        @NotNull Long total
) {
    public int size() {
        return list.size();
    }
}
