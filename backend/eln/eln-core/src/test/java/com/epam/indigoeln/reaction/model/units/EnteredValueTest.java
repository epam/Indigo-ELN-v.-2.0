package com.epam.indigoeln.reaction.model.units;

import one.util.streamex.StreamEx;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userEntered;
import static com.epam.indigoeln.reaction.model.units.EnteredValueOpt.opt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class EnteredValueTest {

    private static final List<MeasurementUnit> UNITS = StreamEx.<MeasurementUnit[]>of(
            MolUnit.values(),
            MolWeightUnit.values(),
            VolumeUnit.values(),
            WeightUnit.values(),
            MolarityUnit.values(),
            DensityUnit.values(),
            NoUnit.values()
    ).flatMap(Arrays::stream).toList();

    private static final Map<String, MeasurementUnit> UNIT_NAMES = StreamEx.of(UNITS)
            .toMap(MeasurementUnit::name, Function.identity());

    private static final Offset<Double> EPSILON = Offset.offset(0.0001);

    @Test
    void testAddUnitsCombinations() {
        assertSoftly(softly -> {
            for (MeasurementUnit unitA : UNITS) {
                for (MeasurementUnit unitB : UNITS) {
                    AbstractThrowableAssert<?, ? extends Throwable> assertion = softly.assertThatCode(() -> {
                        EnteredValueOpt<MeasurementUnit> valueA = opt(userEntered(1.0, unitA, 1));
                        EnteredValue<MeasurementUnit> valueB = userEntered(1.0, unitB, 1);
                        valueA.add(valueB);
                    }).describedAs("units: %s + %s", unitA, unitB);
                    boolean shouldSucceed = unitA.getClass() == unitB.getClass();
                    if (shouldSucceed) {
                        assertion.doesNotThrowAnyException();
                    } else {
                        assertion.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Inconvertible units");
                    }
                }
            }
        });
    }

    @Test
    void testAddUnitsSuccess() {
        EnteredValue<WeightUnit> valueA = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<WeightUnit> valueB = userEntered(500.0, WeightUnit.MG, 1);
        EnteredValue<WeightUnit> result = EnteredValue.add(valueA, valueB);
        assertThat(result.getUnit()).isEqualTo(WeightUnit.MG);
        assertThat(result.getValue()).isCloseTo(1500.0, EPSILON);
    }

    @Test
    void testAddInconvertibleUnitsThrows() {
        EnteredValue<MeasurementUnit> weight = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<MeasurementUnit> volume = userEntered(1.0, VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> opt(weight).add(volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @Test
    void testSubtractUnitsSuccess() {
        EnteredValue<WeightUnit> valueA = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<WeightUnit> valueB = userEntered(500.0, WeightUnit.MG, 1);
        EnteredValue<WeightUnit> result = EnteredValue.subtract(valueA, valueB);
        assertThat(result.getUnit()).isEqualTo(WeightUnit.MG);
        assertThat(result.getValue()).isCloseTo(500.0, EPSILON);
    }

    @Test
    void testSubtractInconvertibleUnitsThrows() {
        EnteredValue<MeasurementUnit> weight = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<MeasurementUnit> volume = userEntered(1.0, VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> opt(weight).subtract(volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @Test
    void testMultiplyScalarSuccess() {
        EnteredValue<WeightUnit> self = userEntered(2.0, WeightUnit.G, 1);
        EnteredValue<WeightUnit> result = EnteredValue.multiply(self, 3.0);
        assertThat(result.getUnit()).isEqualTo(WeightUnit.G);
        assertThat(result.getValue()).isCloseTo(6.0, EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "G,NO_UNIT,G,1",
            "MM,ML,UMOL,1",
            "MM,L,MMOL,1",
            "M,ML,MMOL,1",
            "M,L,MOL,1",
            "MOL,G_PER_MOL,G,1",
            "MMOL,G_PER_MOL,MG,1",
            "UMOL,G_PER_MOL,MG,0.001",
            "ML,G_ML,G,1",
            "L,G_ML,KG,1",
    })
    void testMultiplySuccess(String unitA, String unitB, String expectedUnit, double expectedValue) {
        EnteredValueOpt<MeasurementUnit> valueA = opt(userEntered(1.0, getUnit(unitA), 1));
        EnteredValue<MeasurementUnit> valueB = userEntered(1.0, getUnit(unitB), 1);
        EnteredValue<MeasurementUnit> result = valueA.multiply(valueB).getValue();
        assertThat(result.getUnit()).isEqualTo(getUnit(expectedUnit));
        assertThat(result.getValue()).isCloseTo(expectedValue, EPSILON);
    }

    @Test
    void testMultiplyInconvertibleUnitsThrows() {
        EnteredValue<WeightUnit> weight = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<VolumeUnit> volume = userEntered(1.0, VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> opt(weight).multiply(volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot multiply units: G and ML");
    }

    @Test
    void testDivideScalarSuccess() {
        EnteredValue<WeightUnit> self = userEntered(6.0, WeightUnit.G, 1);
        EnteredValue<WeightUnit> result = EnteredValue.divide(self, 3.0);
        assertThat(result.getUnit()).isEqualTo(WeightUnit.G);
        assertThat(result.getValue()).isCloseTo(2.0, EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "G,G,NO_UNIT,1",
            "G,NO_UNIT,G,1",
            "KG,G,NO_UNIT,1000",
            "MG,G_PER_MOL,MMOL,1",
            "G,G_PER_MOL,MOL,1",
            "KG,G_PER_MOL,MOL,1000",
            "MG,G_ML,ML,0.001",
            "G,G_ML,ML,1",
            "KG,G_ML,L,1",
            "UMOL,MM,ML,1",
            "UMOL,M,ML,0.001",
            "MMOL,MM,ML,1000",
            "MMOL,M,ML,1",
            "MOL,MM,L,1000",
            "MOL,M,L,1",
    })
    void testDivideSuccess(String unitA, String unitB, String expectedUnit, double expectedValue) {
        EnteredValueOpt<MeasurementUnit> valueA = opt(userEntered(1.0, getUnit(unitA), 1));
        EnteredValue<MeasurementUnit> valueB = userEntered(1.0, getUnit(unitB), 1);
        EnteredValue<MeasurementUnit> result = valueA.divide(valueB).getValue();
        assertThat(result.getUnit()).isEqualTo(getUnit(expectedUnit));
        assertThat(result.getValue()).isCloseTo(expectedValue, EPSILON);
    }

    @Test
    void testDivideInconvertibleUnitsThrows() {
        EnteredValue<WeightUnit> weight = userEntered(1.0, WeightUnit.G, 1);
        EnteredValue<VolumeUnit> volume = userEntered(1.0, VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> opt(weight).divide(volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot divide units: G and ML");
    }

    private static MeasurementUnit getUnit(String name) {
        MeasurementUnit unit = UNIT_NAMES.get(name);
        if (unit == null) {
            throw new IllegalArgumentException("Unknown unit: " + name);
        }
        return unit;
    }
}
