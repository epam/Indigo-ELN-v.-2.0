package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.ValueHandler;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings({"OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
abstract class AbstractValueHandler<C, T, P> implements ValueHandler<C, T, P> {

    @Nullable
    @Override
    public Optional<P> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from) {
        Flag selfUpdated = new Flag();
        if (a == null && b == null) {
            return null;
        }
        if (b == null) {
            updated.set();
            return Optional.empty();
        }
        P patch = doCompare(selfUpdated, a, b, from);
        if (selfUpdated.isSet()) {
            updated.set();
            return Optional.of(patch);
        }
        return null;
    }

    @Nullable
    @Override
    public T apply(C container, @Nullable T value, @Nullable Optional<P> patch) {
        if (patch == null) {
            return value;
        }
        //noinspection OptionalIsPresent
        if (patch.isEmpty()) {
            return null;
        }
        return doApply(container, value, patch.get());
    }

    // outcome:
    //     updated == false -> ignore result, nothing changed
    //     otherwise -> use returned patch
    protected abstract P doCompare(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from);

    protected abstract T doApply(C container, @Nullable T value, P patch);
}
