package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.metamodel.ModelProperty;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.reaction.util.PatchUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class AbstractMetamodelValueHandler<O, C, P> extends AbstractValueHandler<O, C, P> {

    private final Metamodel<C, P> metamodel;
    private final Supplier<P> patchCreator;

    @Override
    protected P doCompare(Flag updated, @Nullable C a, C b, @Nullable Optional<Integer> from) {
        P patch = patchCreator.get();
        doCompareBase(updated, a, b, patch);
        return patch;
    }

    protected void doCompareBase(Flag updated, @Nullable C a, C b, P patch) {
        for (ModelProperty<C, ?, P, ?> property : metamodel.getProperties()) {
            ModelProperty<C, Object, P, Object> simpleProperty = property.cast();
            Optional<Object> diffValue = PatchUtil.diff(updated, a, b, simpleProperty.defaultValue(), simpleProperty.getter(), simpleProperty.valueHandler());
            simpleProperty.patchSet(patch, diffValue);
        }
    }

    @Override
    protected C doApply(O container, @Nullable C value, P patch) {
        if (value == null) {
            value = createNewValue(container, patch);
        }
        doApplyBase(value, patch);
        return value;
    }

    protected abstract C createNewValue(O container, P patch);

    protected void doApplyBase(C value, P patch) {
        for (ModelProperty<C, ?, P, ?> property : metamodel.getProperties()) {
            ModelProperty<C, Object, P, Object> simpleProperty = property.cast();
            BiConsumer<C, Object> setter = simpleProperty.setter();
            if (setter != null) {
                PatchUtil.restore(value, simpleProperty.patchGet(patch), simpleProperty.getter(), setter, simpleProperty.valueHandler());
            }
        }
    }
}
