package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.reaction.model.EnteredValue.calculated;
import static com.epam.indigoeln.reaction.model.EnteredValue.empty;
import static com.epam.indigoeln.reaction.util.UnitUtils.divisionUnit;
import static com.epam.indigoeln.reaction.util.UnitUtils.multiplicationUnit;

@FunctionalInterface
public interface Calculation<U extends MeasurementUnit> {

    EnteredValue<U> get();

    static <U extends MeasurementUnit> Calculation<U> readOnly(EnteredValue<U> enteredValue) {
        return () -> enteredValue;
    }

    default Calculation<U> add(Calculation<U> other) {
        return () -> {
            Result2<U, U> parts = calculateBoth(this, other);
            if (parts == null) {
                return empty();
            }
            Preconditions.checkArgument(parts.a().getUnit().getBaseUnit() == parts.b().getUnit().getBaseUnit(), "Inconvertible units: %s and %s", parts.a.getUnit(), parts.b.getUnit());
            return calculated(parts.a.getBaseValue() + parts.b.getBaseValue(), parts.a.getUnit().getBaseUnit());
        };
    }

    default Calculation<U> subtract(Calculation<U> other) {
        return () -> {
            Result2<U, U> parts = calculateBoth(this, other);
            if (parts == null) {
                return empty();
            }
            Preconditions.checkArgument(parts.a.getUnit().getBaseUnit() == parts.b.getUnit().getBaseUnit(), "Inconvertible units: %s and %s", parts.a.getUnit(), parts.b.getUnit());
            return calculated(parts.a.getBaseValue() - parts.b.getBaseValue(), parts.a.getUnit().getBaseUnit());
        };
    }

    default <R extends MeasurementUnit> Calculation<R> multiply(Calculation<?> other) {
        return () -> {
            Result2<U, ?> parts = calculateBoth(this, other);
            if (parts == null) {
                return empty();
            }
            return calculated(parts.a.getBaseValue() * parts.b.getBaseValue(), multiplicationUnit(parts.a.getUnit(), parts.b.getUnit()));
        };
    }

    default Calculation<U> multiply(double by) {
        return () -> {
            EnteredValue<U> thisResult = get();
            return thisResult.hasValue() ? calculated(thisResult.getBaseValue() * by, thisResult.getUnit()) : empty();
        };
    }

    default <R extends MeasurementUnit> Calculation<R> divide(Calculation<?> other) {
        return () -> {
            Result2<U, ?> parts = calculateBoth(this, other);
            if (parts == null || parts.b.getBaseValue() == 0.0) {
                return empty();
            }
            return calculated(parts.a.getBaseValue() / parts.b.getBaseValue(), divisionUnit(parts.a.getUnit(), parts.b.getUnit()));
        };
    }

    default Calculation<NoUnit> percentageOf(Calculation<U> total) {
        return () -> {
            EnteredValue<U> thisResult = get();
            if (!thisResult.hasValue()) {
                return empty();
            }
            EnteredValue<U> totalResult = total.get();
            if (!totalResult.hasValue() || totalResult.getBaseValue() == 0.0) {
                return empty();
            }
            return calculated(thisResult.getBaseValue() / totalResult.getBaseValue() * 100.0, NoUnit.NO_UNIT);
        };
    }

    @Nullable
    private static <A extends MeasurementUnit, B extends MeasurementUnit> Result2<A, B> calculateBoth(Calculation<A> a, Calculation<B> b) {
        EnteredValue<A> aResult = a.get();
        if (!aResult.hasValue()) {
            return null;
        }
        EnteredValue<B> bResult = b.get();
        if (!bResult.hasValue()) {
            return null;
        }
        return new Result2<>(aResult, bResult);
    }

    static <R extends MeasurementUnit> Calculation<R> sum(List<? extends Calculation<R>> list, R unit) {
        return () -> {
            double baseSum = 0.0;
            for (Calculation<R> item : list) {
                EnteredValue<R> itemResult = item.get();
                if (!itemResult.hasValue()) {
                    return empty();
                }
                Preconditions.checkArgument(itemResult.getUnit().getBaseUnit() == unit);
                baseSum += itemResult.getBaseValue();
            }
            return calculated(baseSum, unit);
        };
    }

    record Result2<A extends MeasurementUnit, B extends MeasurementUnit> (
            EnteredValue<A> a,
            EnteredValue<B> b
    ) {}
}
