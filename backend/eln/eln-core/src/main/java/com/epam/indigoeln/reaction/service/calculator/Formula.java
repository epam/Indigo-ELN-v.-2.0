package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.EnteredValue;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class Formula<U extends MeasurementUnit> {

    final String name;

    final CalculableProperty<?, U> target;

    final Calculation<U> supplier;

    EnteredValue<U> value = EnteredValue.empty();

    @Nullable
    EnteredValue<U> snapshot;

    Formula(String name, CalculableProperty<?, U> target, Calculation<U> supplier, CalculableProperty<?, ?>... sources) {
        this.name = name;
        this.target = target;
        this.supplier = supplier;
        addSources(Arrays.asList(sources));
    }

    public void addSources(Collection<? extends CalculableProperty<?, ?>> sources) {
        for (CalculableProperty<?, ?> source : sources) {
            source.getDownstream().add(this);
        }
    }

    public void snapshot() {
        snapshot = value;
    }

    public void revert() {
        value = checkNotNull(snapshot);
    }

    @Override
    public String toString() {
        return name;
    }
}
