package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

public class Formula<U extends MeasurementUnit> {

    final String name;

    final EnteredValueOpt.Property<?, U> target;

    final Supplier<EnteredValueOpt<U>> supplier;

    @Nullable
    EnteredValue<U> value;

    @Nullable
    EnteredValue<U> snapshot;

    Formula(String name, EnteredValueOpt.Property<?, U> target, Supplier<EnteredValueOpt<U>> supplier, EnteredValueOpt.Property<?, ?>... sources) {
        this.name = name;
        this.target = target;
        this.supplier = supplier;
        addSources(Arrays.asList(sources));
    }

    public Formula<U> addSources(Collection<? extends EnteredValueOpt.Property<?, ?>> sources) {
        for (EnteredValueOpt.Property<?, ?> source : sources) {
            source.getDownstream().add(this);
        }
        return this;
    }

    public void snapshot() {
        snapshot = value;
    }

    public void revert() {
        value = snapshot;
    }

    @Override
    public String toString() {
        return name;
    }
}
