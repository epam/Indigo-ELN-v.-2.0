package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.metamodel.ModelProperty;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.reaction.util.PatchUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class AbstractMetamodelValueHandler<C, T, P> extends AbstractValueHandler<C, T, P> {

    private final Metamodel<T, P> metamodel;
    private final Supplier<P> patchCreator;

    @Override
    protected P doCompare(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from) {
        P patch = patchCreator.get();
        doCompareBase(updated, a, b, patch);
        return patch;
    }

    protected void doCompareBase(Flag updated, @Nullable T a, T b, P patch) {
        for (ModelProperty<T, ?, P, ?> property : metamodel.getProperties()) {
            ModelProperty<T, Object, P, Object> simpleProperty = property.cast();
            doCompareProperty(updated, a, b, patch, simpleProperty);
        }
    }

    protected <T, P> void doCompareProperty(Flag updated, @Nullable T a, T b, P patch, ModelProperty<T, Object, P, Object> simpleProperty) {
        Optional<Object> diffValue = PatchUtil.diff(updated, a, b, simpleProperty.defaultValue(), simpleProperty.getter(), simpleProperty.valueHandler());
        simpleProperty.patchSet(patch, diffValue);
    }
}
