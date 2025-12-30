package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.util.Flag;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class MetamodelValueHandler<C, T, P> extends AbstractMetamodelValueHandler<C, T, P> {

    public MetamodelValueHandler(Metamodel<T, P> metamodel, Supplier<P> patchCreator) {
        super(metamodel, patchCreator);
    }

    @Override
    protected void doCompareBase(Flag updated, @Nullable T a, T b, P patch) {
        super.doCompareBase(updated, a, b, patch);
    }
}
