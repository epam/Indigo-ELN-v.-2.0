package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.reaction.model.units.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.math3.util.Precision;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;

@SuppressWarnings("UnusedReturnValue")
public class EnteredValueAssert<U extends MeasurementUnit> extends AbstractAssert<EnteredValueAssert<U>, EnteredValue<U>> {

    private static final Offset<Double> EPSILON = Offset.offset(0.0001);

    public static <U extends MeasurementUnit> EnteredValueAssert<U> assertThat(EnteredValue<U> actual) {
        return new EnteredValueAssert<>(actual);
    }

    protected EnteredValueAssert(EnteredValue<U> actual) {
        super(actual, EnteredValueAssert.class);
    }

    public EnteredValueAssert<U> isEmpty() {
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.isEmpty()).describedAs(actual::toString).isTrue();
        return this;
    }

    public EnteredValueAssert<U> hasValue(double value) {
        Assertions.assertThat(actual != null && !actual.isEmpty()).describedAs("was empty").isTrue();
        if (!(actual.getUnit() instanceof MolWeightUnit) && !(actual.getUnit() instanceof NoUnit) && !(actual.getUnit() instanceof DensityUnit)) {
            throw new IllegalStateException("Must use hasValue(value, unit) for " + actual.getUnit().getClass().getSimpleName());
        }
        Assertions.assertThat(actual.getValue()).describedAs(actual::toString).isCloseTo(value, EPSILON);
        return this;
    }

    public EnteredValueAssert<U> hasValue(double value, U unit) {
        Assertions.assertThat(actual != null && !actual.isEmpty()).describedAs("was empty").isTrue();
        if (!Precision.equalsWithRelativeTolerance(actual.getValue(), value, 1e-6) || actual.getUnit() != unit) {
            String description = Strings.isNullOrEmpty(descriptionText()) ? actual.toString() : descriptionText();
            failWithMessage("[%s]\nexpected: %s %s\n but was: %s %s", description, value, unit, actual.getStringValue(), actual.getUnit());
        }
        return this;
    }

    public EnteredValueAssert<U> hasStringValue(String stringValue) {
        Assertions.assertThat(actual != null && !actual.isEmpty()).describedAs("was empty").isTrue();
        Assertions.assertThat(actual.getStringValue()).describedAs(actual::toString).isEqualTo(stringValue);
        return this;
    }

    public EnteredValueAssert<U> isUserEntered() {
        Assertions.assertThat(actual != null && !actual.isEmpty()).describedAs("was empty").isTrue();
        Assertions.assertThat(actual.getSource().isUserEntered()).describedAs(actual::toString).isTrue();
        return this;
    }

    public EnteredValueAssert<U> isUserEntered(int priority) {
        isUserEntered();
        Assertions.assertThat(Preconditions.checkNotNull(actual).getSource().getPriority()).describedAs(actual::toString).isEqualTo(priority);
        return this;
    }

    public EnteredValueAssert<U> isOverwritten() {
        Assertions.assertThat(actual.isOverwritten()).describedAs(actual::toString).isTrue();
        return this;
    }

    public EnteredValueAssert<U> isNotOverwritten() {
        Assertions.assertThat(actual.isOverwritten()).describedAs(actual::toString).isFalse();
        return this;
    }
}
