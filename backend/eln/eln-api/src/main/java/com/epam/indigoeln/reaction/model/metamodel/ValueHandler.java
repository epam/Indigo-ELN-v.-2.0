package com.epam.indigoeln.reaction.model.metamodel;

import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// C - item itself (e.g. Reaction)
// T - value type (e.g. String or EnteredValue)
// P - patch value type (e.g. String or EnteredValuePatch)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface ValueHandler<C, T, P> {

    @Nullable
    Optional<P> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from);

    @Nullable
    T apply(C container, @Nullable T value, @Nullable Optional<P> patch);
}
