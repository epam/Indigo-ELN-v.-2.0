package com.epam.indigoeln.common.util;

public record Pair<A extends Object, B extends Object>(A a, B b) {

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }
}
