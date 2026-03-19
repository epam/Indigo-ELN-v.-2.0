package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static com.epam.indigoeln.eln.util.PatchUtil.diff;

@RequiredArgsConstructor
public class EnteredValueDiffHandler<U extends MeasurementUnit> extends AbstractDiffHandler<EnteredValue<U>, EnteredValuePatch<U>> {

    private static final EnteredValueDiffHandler<NoUnit> INSTANCE = new EnteredValueDiffHandler<>(null, null);

    @Getter
    @Nullable
    private final EnteredValue<U> defaultValue;

    @Getter
    @Nullable
    private final EnteredValue<U> defaultValueForCalculations;

    public static <U extends MeasurementUnit> EnteredValueDiffHandler<U> instance() {
        //noinspection unchecked
        return (EnteredValueDiffHandler<U>) INSTANCE;
    }

    @Override
    protected boolean isEmpty(@Nullable EnteredValue<U> value) {
        return value == null || Objects.equals(value, defaultValue);
    }

    @Override
    @Nullable
    protected Patched<EnteredValue<U>, EnteredValuePatch<U>> doCompare(@Nullable EnteredValue<U> a, EnteredValue<U> b) {
        EnteredValuePatch<U> patch = new EnteredValuePatch<>();
        Flag updated = new Flag();
        patch.setValue(diff(updated, a, b, EnteredValue::getStringValue));
        patch.setUnit(diff(updated, a, b, EnteredValue::getUnit));
        // not nice to do it in patch handler, but this is to preserve revision of calculated values;
        // otherwise calculated values would always have the most recent revision number, as they are recalculated each time
        if (a != null && patch.getValue() == null && patch.getUnit() == null && a.getSource().isCalculated() && b.getSource().isCalculated()) {
            b.setSource(a.getSource());
        }
        patch.setSource(diff(updated, a, b, EnteredValue::getSource));
        patch.setOverwritten(diff(updated, a, b, EnteredValue::isOverwritten, DefaultDiffHandler.DEFAULT_FALSE_INSTANCE));
        return updated.isSet() ? Patched.updated(patch) : null;
    }
}
