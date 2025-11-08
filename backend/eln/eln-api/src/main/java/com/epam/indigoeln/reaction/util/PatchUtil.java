package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.metamodel.ValueHandler;
import com.epam.indigoeln.reaction.model.patch.handler.DefaultValueHandler;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("OptionalAssignedToNull")
public class PatchUtil {

    @Nullable
    public static <T> Optional<T> diff(Flag updated, @Nullable T a, @Nullable T b) {
        return diff(updated, a, b, DefaultValueHandler.instance());
    }

    @Nullable
    public static <T, P> Optional<P> diff(Flag updated, @Nullable T a, @Nullable T b, ValueHandler<?, T, P> handler) {
        return handler.compare(updated, a, b, null);
    }

    @Nullable
    public static <C, T> Optional<T> diff(Flag updated, @Nullable C a, @Nullable C b, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, valueFn, DefaultValueHandler.instance());
    }

    @Nullable
    public static <C, T> Optional<T> diff(Flag updated, @Nullable C a, @Nullable C b, @Nullable T defaultValue, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, defaultValue, valueFn, DefaultValueHandler.instance());
    }

    @Nullable
    public static <C, T, P> Optional<P> diff(Flag updated, @Nullable C a, @Nullable C b, Function<C, @Nullable T> valueFn, ValueHandler<C, T, P> handler) {
        return diff(updated, a, b, null, valueFn, handler);
    }

    @Nullable
    public static <C, T, P> Optional<P> diff(Flag updated, @Nullable C a, @Nullable C b, @Nullable T defaultValue, Function<C, @Nullable T> valueFn, ValueHandler<?, T, P> handler) {
        return handler.compare(updated, a != null ? valueFn.apply(a) : defaultValue, b != null ? valueFn.apply(b) : null, null);
    }

    public static <C, T> void restore(C target, @Nullable Optional<T> patch, BiConsumer<C, T> setterFn) {
        restore(target, patch, (x) -> null, setterFn, DefaultValueHandler.instance());
    }

    public static <C, T, P> void restore(C container, @Nullable Optional<P> patch, Function<C, @Nullable T> getterFn, BiConsumer<C, T> setterFn, ValueHandler<C, T, P> valueHandler) {
        T value = getterFn.apply(container);
        T newValue = valueHandler.apply(container, value, patch);
        if (newValue != value) {
            setterFn.accept(container, newValue);
        }
    }
}
