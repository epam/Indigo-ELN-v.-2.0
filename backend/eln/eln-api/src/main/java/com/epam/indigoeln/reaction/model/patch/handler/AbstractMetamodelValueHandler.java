package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.metamodel.ModelProperty;
import com.epam.indigoeln.reaction.model.patch.AbstractListElementPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.reaction.util.PatchUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
abstract class AbstractMetamodelValueHandler<O, C, P> extends AbstractValueHandler<O, C, P> {

    private final Metamodel<C, P> metamodel;

    protected void doSetFrom(Flag updated, @Nullable Optional<Integer> from, AbstractListElementPatch<?> patch) {
        if (from != null) {
            patch.setXfrom(from);
            updated.set();
        }
    }

    protected void doCompareBase(Flag updated, @Nullable C a, C b, @Nullable Optional<Integer> from, P patch) {
        for (ModelProperty<C, ?, P, ?> property : metamodel.getProperties()) {
            ModelProperty<C, Object, P, Object> simpleProperty = property.cast();
            Optional<Object> diffValue = PatchUtil.diff(updated, a, b, simpleProperty.defaultValue(), simpleProperty.getter(), simpleProperty.valueHandler());
            simpleProperty.patchSet(patch, diffValue);
        }
    }

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
