package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.eln.util.PatchUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class MetamodelDiffHandler<T, P> extends AbstractDiffHandler<T, P> {

    private final Metamodel<T, P> metamodel;
    private final Supplier<P> patchCreator;

    @Override
    @Nullable
    protected Patched<P> doCompare(@Nullable T a, T b) {
        P patch = patchCreator.get();
        Flag updated = new Flag();
        doCompareBase(updated, a, b, patch);
        return updated.isSet() ? Patched.verbatim(patch) : null;
    }

    protected void doCompareBase(Flag updated, @Nullable T a, T b, P patch) {
        for (ModelProperty<T, ?, P, ?> property : metamodel.getProperties()) {
            ModelProperty<T, Object, P, Object> simpleProperty = property.cast();
            doCompareProperty(updated, a, b, patch, simpleProperty);
        }
    }

    protected void doCompareProperty(Flag updated, @Nullable T a, T b, P patch, ModelProperty<T, Object, P, Object> simpleProperty) {
        Patched<Object> diffValue = PatchUtil.diff(updated, a, b, simpleProperty.getter(), simpleProperty.defaultValue(), simpleProperty.valueHandler());
        simpleProperty.patchSet(patch, diffValue);
    }
}
