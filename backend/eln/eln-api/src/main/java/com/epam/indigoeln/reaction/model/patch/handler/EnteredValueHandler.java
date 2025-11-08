package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.metamodel.ValueHandler;
import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.reaction.util.PatchUtil;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalAssignedToNull")
public class EnteredValueHandler<C, U extends MeasurementUnit> extends AbstractValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> {

    private static final EnteredValueHandler<Object, NoUnit> INSTANCE = new EnteredValueHandler<>();

    public static <C, U extends MeasurementUnit> ValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> instance() {
        //noinspection unchecked,rawtypes
        return (ValueHandler) INSTANCE;
    }

    @Override
    public EnteredValuePatch<U> doCompare(Flag updated, @Nullable EnteredValue<U> a, EnteredValue<U> b, @Nullable Optional<Integer> from) {
        EnteredValuePatch<U> patch = new EnteredValuePatch<>();
        patch.setValue(PatchUtil.diff(updated, a, b, EnteredValue::getValue));
        patch.setUnit(PatchUtil.diff(updated, a, b, EnteredValue::getUnit));
        patch.setSource(PatchUtil.diff(updated, a, b, EnteredValue::getSource));
        patch.setConflict(PatchUtil.diff(updated, a, b, false, EnteredValue::isConflict));
        return patch;
    }

    @Override
    protected EnteredValue<U> doApply(C container, @Nullable EnteredValue<U> value, EnteredValuePatch<U> patch) {
        Double v = value != null ? value.getValue() : null;
        U u = value != null ? value.getUnit() : null;
        EnteredValueSource s = value != null ? value.getSource() : null;
        boolean c = value != null && value.isConflict();
        if (patch.getValue() != null) {
            Preconditions.checkState(patch.getValue().isPresent());
            v = patch.getValue().get();
        }
        if (patch.getUnit() != null) {
            Preconditions.checkState(patch.getUnit().isPresent());
            u = patch.getUnit().get();
        }
        if (patch.getSource() != null) {
            s = patch.getSource().orElse(null);
        }
        if (patch.getConflict() != null) {
            Preconditions.checkState(patch.getConflict().isPresent());
            c = patch.getConflict().get();
        }
        Preconditions.checkState(v != null && u != null && s != null);
        EnteredValue<U> result = new EnteredValue<>(v, u, s);
        result.setConflict(c);
        return result;
    }
}
