package com.epam.indigoeln.common.util;

import org.jspecify.annotations.Nullable;

public record Pair<A extends @Nullable Object, B extends @Nullable Object>(A a, B b) {

    public static <A extends @Nullable Object, B extends @Nullable Object> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }
}
