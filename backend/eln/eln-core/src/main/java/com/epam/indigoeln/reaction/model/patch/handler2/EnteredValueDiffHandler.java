package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.Flag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.eln.util.PatchUtil.diff;

@RequiredArgsConstructor
public class EnteredValueDiffHandler<U extends MeasurementUnit> extends AbstractDiffHandler<EnteredValue<U>, EnteredValuePatch<U>> {

    private static final EnteredValueDiffHandler<NoUnit> INSTANCE = new EnteredValueDiffHandler<>(null);

    private final EnteredValue<U> defaultValue;

    public static <U extends MeasurementUnit> EnteredValueDiffHandler<U> instance() {
        //noinspection unchecked
        return (EnteredValueDiffHandler<U>) INSTANCE;
    }

    @Override
    protected boolean isEmpty(@Nullable EnteredValue<U> value) {
        return value == null || value.equals(defaultValue);
    }

    @Override
    @Nullable
    protected Patched<EnteredValuePatch<U>> doCompare(@Nullable EnteredValue<U> a, EnteredValue<U> b) {
        EnteredValuePatch<U> patch = new EnteredValuePatch<>();
        Flag updated = new Flag();
        patch.setValue(diff(updated, a, b, EnteredValue::getValue)); // !!! precision???
        patch.setUnit(diff(updated, a, b, EnteredValue::getUnit));
        patch.setSource(diff(updated, a, b, EnteredValue::getSource));
        patch.setConflict(diff(updated, a, b, EnteredValue::isConflict, false));
        return updated.isSet() ? Patched.verbatim(patch) : null;
    }
}
