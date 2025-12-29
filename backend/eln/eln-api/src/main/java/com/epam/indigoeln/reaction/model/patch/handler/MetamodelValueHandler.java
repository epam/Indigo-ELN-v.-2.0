package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MetamodelValueHandler<C, T, P> extends AbstractMetamodelValueHandler<C, T, P> {

    private final BiFunction<C, P, T> valueCreator;

    public MetamodelValueHandler(Metamodel<T, P> metamodel, Supplier<P> patchCreator, BiFunction<C, P, T> valueCreator) {
        super(metamodel, patchCreator);
        this.valueCreator = valueCreator;
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable T a, T b, P patch) {
        super.doCompareBase(updated, a, b, patch);
    }

    @Override
    protected T createNewValue(C container, P patch) {
        return valueCreator.apply(container, patch);
    }
}
