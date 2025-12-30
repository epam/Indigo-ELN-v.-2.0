package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.AbstractListElementPatch;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class MetamodelListItemValueHandler<C, T, A, P extends AbstractListElementPatch<A>> extends AbstractMetamodelValueHandler<C, T, P> {

    private final BiFunction<C, A, T> valueCreator;

    public MetamodelListItemValueHandler(Metamodel<T, P> metamodel, Supplier<P> patchCreator, BiFunction<C, A, T> valueCreator) {
        super(metamodel, patchCreator);
        this.valueCreator = valueCreator;
    }

    @Override
    protected P doCompare(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from) {
        P patch = super.doCompare(updated, a, b, from);
        doSetFrom(updated, from, patch);
        return patch;
    }

    protected void doSetFrom(Flag updated, @Nullable Optional<Integer> from, AbstractListElementPatch<?> patch) {
        if (from != null) {
            patch.setXfrom(from);
            updated.set();
        }
    }
}
