package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.util.Flag;
import com.epam.indigoeln.reaction.util.PatchUtil;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalAssignedToNull")
public class EnteredValueHandler<C, U extends MeasurementUnit> extends AbstractValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> {

    @Override
    public EnteredValuePatch<U> doCompare(Flag updated, @Nullable EnteredValue<U> a, EnteredValue<U> b, @Nullable Optional<Integer> from) {
        EnteredValuePatch<U> patch = new EnteredValuePatch<>();
        patch.setValue(PatchUtil.diff(updated, a, b, EnteredValue::getValue));
        patch.setUnit(PatchUtil.diff(updated, a, b, EnteredValue::getUnit));
        patch.setSource(PatchUtil.diff(updated, a, b, EnteredValue::getSource));
        patch.setConflict(PatchUtil.diff(updated, a, b, false, EnteredValue::isConflict));
        return patch;
    }
}
