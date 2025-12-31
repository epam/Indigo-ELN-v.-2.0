package com.epam.indigoeln.reaction.model.patch.handler2;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class DefaultDiffHandler<T> extends AbstractDiffHandler<T, T> {

    private static final DefaultDiffHandler<Object> INSTANCE = new DefaultDiffHandler<>();

    public static <T> DefaultDiffHandler<T> instance() {
        //noinspection unchecked
        return (DefaultDiffHandler<T>) INSTANCE;
    }

    @Override
    protected boolean doEquals(T a, T b) {
        return Objects.equals(a, b);
    }

    @Override
    protected T doVerbatim(T value) {
        return value;
    }

    @Override
    @Nullable
    protected Patched<T> doCompare(T a, T b) {
        return Patched.updated(a, b);
    }
}
