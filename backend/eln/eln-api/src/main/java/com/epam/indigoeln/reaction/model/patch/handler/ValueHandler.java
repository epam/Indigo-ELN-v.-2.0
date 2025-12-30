package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// C - parent container (e.g. Reaction)
// T - value type (e.g. String or EnteredValue)
// P - patch value type (e.g. String or EnteredValuePatch)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface ValueHandler<C, T, P> {

    @Nullable
    Optional<P> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from);
}
