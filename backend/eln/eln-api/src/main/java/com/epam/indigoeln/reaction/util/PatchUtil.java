package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.patch.handler.ValueHandler;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
public class PatchUtil {

    @Nullable
    public static <C, T> Optional<T> diff(Flag updated, @Nullable C a, @Nullable C b, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, valueFn, Handlers.defaultHandler());
    }

    @Nullable
    public static <C, T> Optional<T> diff(Flag updated, @Nullable C a, @Nullable C b, @Nullable T defaultValue, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, defaultValue, valueFn, Handlers.defaultHandler());
    }

    @Nullable
    public static <C, T, P> Optional<P> diff(Flag updated, @Nullable C a, @Nullable C b, Function<C, @Nullable T> valueFn, ValueHandler<C, T, P> handler) {
        return diff(updated, a, b, null, valueFn, handler);
    }

    @Nullable
    public static <C, T, P> Optional<P> diff(Flag updated, @Nullable C a, @Nullable C b, @Nullable T defaultValue, Function<C, @Nullable T> valueFn, ValueHandler<?, T, P> handler) {
        return handler.compare(updated, a != null ? valueFn.apply(a) : defaultValue, b != null ? valueFn.apply(b) : null, null);
    }
}
