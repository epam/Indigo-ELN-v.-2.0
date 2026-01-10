package com.epam.indigoeln.reaction.model.patch.handler2;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@RequiredArgsConstructor
public class DefaultDiffHandler<T> extends AbstractDiffHandler<T, T> {

    public static final DefaultDiffHandler<Boolean> DEFAULT_FALSE_INSTANCE = new DefaultDiffHandler<>(false);

    private static final DefaultDiffHandler<Object> INSTANCE = new DefaultDiffHandler<>(null);

    @Nullable
    private final T defaultValue;

    public static <T> DefaultDiffHandler<T> instance() {
        //noinspection unchecked
        return (DefaultDiffHandler<T>) INSTANCE;
    }

    @Override
    protected boolean isEmpty(@Nullable T value) {
        return value == null || value.equals(defaultValue);
    }

    @Override
    protected boolean doEquals(T a, T b) {
        return Objects.equals(a, b);
    }

    @Override
    @Nullable
    protected Patched<T, T> doCompare(T a, T b) {
        return Patched.replaced(a, b);
    }
}
