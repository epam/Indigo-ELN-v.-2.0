package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.model.patch.handler2.DefaultDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class PatchUtil {

    @Nullable
    public static <C, T> Patched<T> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, valueFn, null, DefaultDiffHandler.instance());
    }

    @Nullable
    public static <C, T> Patched<T> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn, @Nullable T defaultValue) {
        return diff(updated, a, b, valueFn, defaultValue, DefaultDiffHandler.instance());
    }

    @Nullable
    public static <C, T, P> Patched<P> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn, DiffHandler<T, P> handler) {
        return diff(updated, a, b, valueFn, null, handler);
    }

    @Nullable
    public static <C, T, P> Patched<P> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn, @Nullable T defaultValue, DiffHandler<T, P> handler) {
        Patched<P> result = handler.compare(a != null ? valueFn.apply(a) : defaultValue, valueFn.apply(b));
        if (result != null) {
            updated.set();
        }
        return result;
    }
}
