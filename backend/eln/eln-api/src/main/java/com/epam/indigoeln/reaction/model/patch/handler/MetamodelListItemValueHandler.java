package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.AbstractListElementPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class MetamodelListItemValueHandler<O, C, A extends Anchor, P extends AbstractListElementPatch<A>> extends AbstractMetamodelValueHandler<O, C, P> {

    private final BiFunction<O, A, C> valueCreator;

    public MetamodelListItemValueHandler(Metamodel<C, P> metamodel, Supplier<P> patchCreator, BiFunction<O, A, C> valueCreator) {
        super(metamodel, patchCreator);
        this.valueCreator = valueCreator;
    }

    @Override
    protected P doCompare(Flag updated, @Nullable C a, C b, @Nullable Optional<Integer> from) {
        P patch = super.doCompare(updated, a, b, from);
        doSetFrom(updated, from, patch);
        return patch;
    }

    @Override
    protected C createNewValue(O container, P patch) {
        Optional<A> anchor = patch.getAnchor();
        Preconditions.checkArgument(anchor != null && anchor.isPresent());
        return valueCreator.apply(container, anchor.get());
    }

    protected void doSetFrom(Flag updated, @Nullable Optional<Integer> from, AbstractListElementPatch<?> patch) {
        if (from != null) {
            patch.setXfrom(from);
            updated.set();
        }
    }
}
