package com.epam.indigoeln.reaction.model.patch.handler2;

import org.jspecify.annotations.Nullable;

// T - value type (e.g. String or EnteredValue)
// P - patch value type (e.g. String or EnteredValuePatch)
public interface DiffHandler<T, P> {

    @Nullable
    Patched<T, P> compare(@Nullable T a, @Nullable T b);
}
