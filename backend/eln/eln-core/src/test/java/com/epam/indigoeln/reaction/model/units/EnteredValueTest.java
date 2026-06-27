package com.epam.indigoeln.reaction.model.units;

import one.util.streamex.StreamEx;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.userEntered;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.callWithSignificantFigures;
import static com.google.common.base.Preconditions.checkArgument;
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

    @RegisterExtension
    static final InvocationInterceptor SIG_FIGS_5 = new InvocationInterceptor() {
        @Override
        public void interceptTestMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> ctx, ExtensionContext ext) throws Throwable {
            callWithSignificantFigures(5, invocation::proceed);
        }
        @Override
        public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> ctx, ExtensionContext ext) throws Throwable {
            callWithSignificantFigures(5, invocation::proceed);
        }
    };

    @Test
    void testAddUnitsCombinations() {
        assertSoftly(softly -> {
            for (MeasurementUnit unitA : UNITS) {
                for (MeasurementUnit unitB : UNITS) {
                    AbstractThrowableAssert<?, ? extends Throwable> assertion = softly.assertThatCode(() -> {
                        EnteredValue<MeasurementUnit> valueA = userEntered("1.0", unitA, 1);
                        EnteredValue<MeasurementUnit> valueB = userEntered("1.0", unitB, 1);
                        EnteredValue.add(valueA, valueB);
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

    @ParameterizedTest
    @CsvSource({
            "1,G,2,G,3,G",
            "1,G,500,MG,1.5,G", // take bigger units (regardless of operand order)
            "500,MG,1,G,1.5,G",
            "0,G,1,MG,1,MG", // except for zero: zero doesn't affect target units (regardless of operand order)
            "1,MG,0,G,1,MG",
            "0,G,0,MG,0,G", // if both operands are zero, take bigger units
    })
    void testAddUnitsSuccess(String aValue, @ConvertWith(UnitConverter.class) WeightUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) WeightUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) WeightUnit expectedUnit) {
        EnteredValue<WeightUnit> valueA = userEntered(aValue, aUnit, 1);
        EnteredValue<WeightUnit> valueB = userEntered(bValue, bUnit, 1);
        EnteredValue<WeightUnit> result = EnteredValue.add(valueA, valueB);
        assertThat(result).hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testAddInconvertibleUnitsThrows() {
        EnteredValue<MeasurementUnit> weight = userEntered("1.0", G, 1);
        EnteredValue<MeasurementUnit> volume = userEntered("1.0", VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> EnteredValue.add(weight, volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @ParameterizedTest
    @CsvSource({
            "3,G,1,G,2,G",
            "1,G,500,MG,0.5,G", // take bigger units (regardless of operand order)
            "1500,MG,1,G,0.5,G",
            "0,G,1,MG,-1,MG", // except for zero: zero doesn't affect target units (regardless of operand order)
            "1,MG,0,G,1,MG",
            "0,G,0,MG,0,G", // if both operands are zero, take bigger units
    })
    void testSubtractUnitsSuccess(String aValue, @ConvertWith(UnitConverter.class) WeightUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) WeightUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) WeightUnit expectedUnit) {
        EnteredValue<WeightUnit> valueA = userEntered(aValue, aUnit, 1);
        EnteredValue<WeightUnit> valueB = userEntered(bValue, bUnit, 1);
        EnteredValue<WeightUnit> result = EnteredValue.subtract(valueA, valueB);
        assertThat(result).hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testSubtractInconvertibleUnitsThrows() {
        EnteredValue<MeasurementUnit> weight = userEntered("1.0", G, 1);
        EnteredValue<MeasurementUnit> volume = userEntered("1.0", VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> EnteredValue.subtract(weight, volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @Test
    void testMultiplyScalarSuccess() {
        EnteredValue<WeightUnit> self = userEntered("2.0", G, 1);
        EnteredValue<WeightUnit> result = EnteredValue.multiply(self, 3.0);
        assertThat(result).hasValue(6, G);
    }

    @ParameterizedTest
    @CsvSource({
            // NO_UNIT left identity
            "NO_UNIT,NO_UNIT,NO_UNIT,1",
            "NO_UNIT,UMOL,UMOL,1",
            "NO_UNIT,MMOL,MMOL,1",
            "NO_UNIT,MOL,MOL,1",
            "NO_UNIT,G_PER_MOL,G_PER_MOL,1",
            "NO_UNIT,ML,ML,1",
            "NO_UNIT,L,L,1",
            "NO_UNIT,MG,MG,1",
            "NO_UNIT,G,G,1",
            "NO_UNIT,KG,KG,1",
            "NO_UNIT,MM,MM,1",
            "NO_UNIT,M,M,1",
            "NO_UNIT,G_ML,G_ML,1",
            // NO_UNIT right identity
            "UMOL,NO_UNIT,UMOL,1",
            "MMOL,NO_UNIT,MMOL,1",
            "MOL,NO_UNIT,MOL,1",
            "G_PER_MOL,NO_UNIT,G_PER_MOL,1",
            "ML,NO_UNIT,ML,1",
            "L,NO_UNIT,L,1",
            "MG,NO_UNIT,MG,1",
            "G,NO_UNIT,G,1",
            "KG,NO_UNIT,KG,1",
            "MM,NO_UNIT,MM,1",
            "M,NO_UNIT,M,1",
            "G_ML,NO_UNIT,G_ML,1",
            // Molarity × Volume
            "MM,ML,UMOL,1",
            "MM,L,MMOL,1",
            "M,ML,MMOL,1",
            "M,L,MOL,1",
            "ML,MM,UMOL,1",
            "L,MM,MMOL,1",
            "ML,M,MMOL,1",
            "L,M,MOL,1",
            // MolWeight × Mol (both orderings)
            "G_PER_MOL,MOL,G,1",
            "G_PER_MOL,MMOL,MG,1",
            "G_PER_MOL,UMOL,MG,0.001",
            "MOL,G_PER_MOL,G,1",
            "MMOL,G_PER_MOL,MG,1",
            "UMOL,G_PER_MOL,MG,0.001",
            // Density × Volume (both orderings)
            "G_ML,ML,G,1",
            "G_ML,L,KG,1",
            "ML,G_ML,G,1",
            "L,G_ML,KG,1",
            // Molarity × MolWeight (both orderings) → Density
            "MM,G_PER_MOL,G_ML,0.000001",
            "M,G_PER_MOL,G_ML,0.001",
            "G_PER_MOL,MM,G_ML,0.000001",
            "G_PER_MOL,M,G_ML,0.001",
    })
    void testMultiplySuccess(@ConvertWith(UnitConverter.class) MeasurementUnit unitA, @ConvertWith(UnitConverter.class) MeasurementUnit unitB, @ConvertWith(UnitConverter.class) MeasurementUnit expectedUnit, double expectedValue) {
        EnteredValue<MeasurementUnit> valueA = userEntered("1.0", unitA, 1);
        EnteredValue<MeasurementUnit> valueB = userEntered("1.0", unitB, 1);
        EnteredValue<MeasurementUnit> result = EnteredValue.multiply(valueA, valueB);
        assertThat(result).hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testMultiplyInconvertibleUnitsThrows() {
        EnteredValue<WeightUnit> weight = userEntered("1.0", G, 1);
        EnteredValue<VolumeUnit> volume = userEntered("1.0", VolumeUnit.ML, 1);
        Assertions.assertThatThrownBy(() -> EnteredValue.multiply(weight, volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot multiply units: G and ML");
    }

    @Test
    void testDivideScalarSuccess() {
        EnteredValue<WeightUnit> self = userEntered("6.0", G, 1);
        EnteredValue<WeightUnit> result = EnteredValue.divide(self, 3.0);
        assertThat(result).hasValue(2, G);
    }

    @ParameterizedTest
    @CsvSource({
            // Same unit → NO_UNIT, 1
            "NO_UNIT,NO_UNIT,NO_UNIT,1",
            "UMOL,UMOL,NO_UNIT,1",
            "MMOL,MMOL,NO_UNIT,1",
            "MOL,MOL,NO_UNIT,1",
            "G_PER_MOL,G_PER_MOL,NO_UNIT,1",
            "ML,ML,NO_UNIT,1",
            "L,L,NO_UNIT,1",
            "MG,MG,NO_UNIT,1",
            "G,G,NO_UNIT,1",
            "KG,KG,NO_UNIT,1",
            "MM,MM,NO_UNIT,1",
            "M,M,NO_UNIT,1",
            "G_ML,G_ML,NO_UNIT,1",
            // Same class, different unit → NO_UNIT, ratio (left.multiplier / right.multiplier)
            "UMOL,MMOL,NO_UNIT,0.001",
            "UMOL,MOL,NO_UNIT,0.000001",
            "MMOL,UMOL,NO_UNIT,1000",
            "MMOL,MOL,NO_UNIT,0.001",
            "MOL,UMOL,NO_UNIT,1000000",
            "MOL,MMOL,NO_UNIT,1000",
            "ML,L,NO_UNIT,0.001",
            "L,ML,NO_UNIT,1000",
            "MG,G,NO_UNIT,0.001",
            "MG,KG,NO_UNIT,0.000001",
            "G,MG,NO_UNIT,1000",
            "G,KG,NO_UNIT,0.001",
            "KG,MG,NO_UNIT,1000000",
            "KG,G,NO_UNIT,1000",
            "MM,M,NO_UNIT,0.001",
            "M,MM,NO_UNIT,1000",
            // Divide by NO_UNIT → same unit, 1
            "UMOL,NO_UNIT,UMOL,1",
            "MMOL,NO_UNIT,MMOL,1",
            "MOL,NO_UNIT,MOL,1",
            "G_PER_MOL,NO_UNIT,G_PER_MOL,1",
            "ML,NO_UNIT,ML,1",
            "L,NO_UNIT,L,1",
            "MG,NO_UNIT,MG,1",
            "G,NO_UNIT,G,1",
            "KG,NO_UNIT,KG,1",
            "MM,NO_UNIT,MM,1",
            "M,NO_UNIT,M,1",
            "G_ML,NO_UNIT,G_ML,1",
            // Weight / MolWeight → Mol
            "MG,G_PER_MOL,MMOL,1",
            "G,G_PER_MOL,MOL,1",
            "KG,G_PER_MOL,MOL,1000",
            // Weight / Density → Volume
            "MG,G_ML,ML,0.001",
            "G,G_ML,ML,1",
            "KG,G_ML,L,1",
            // Weight / Volume → Density
            "MG,ML,G_ML,0.001",
            "MG,L,G_ML,0.000001",
            "G,ML,G_ML,1",
            "G,L,G_ML,0.001",
            "KG,ML,G_ML,1000",
            "KG,L,G_ML,1",
            // Weight / Mol → MolWeight
            "MG,UMOL,G_PER_MOL,1000",
            "MG,MMOL,G_PER_MOL,1",
            "MG,MOL,G_PER_MOL,0.001",
            "G,UMOL,G_PER_MOL,1000000",
            "G,MMOL,G_PER_MOL,1000",
            "G,MOL,G_PER_MOL,1",
            "KG,UMOL,G_PER_MOL,1000000000",
            "KG,MMOL,G_PER_MOL,1000000",
            "KG,MOL,G_PER_MOL,1000",
            // Mol / Molarity → Volume
            "UMOL,MM,ML,1",
            "UMOL,M,ML,0.001",
            "MMOL,MM,ML,1000",
            "MMOL,M,ML,1",
            "MOL,MM,L,1000",
            "MOL,M,L,1",
            // Mol / Volume → Molarity
            "UMOL,ML,MM,1",
            "UMOL,L,MM,0.001",
            "MMOL,ML,M,1",
            "MMOL,L,MM,1",
            "MOL,ML,M,1000",
            "MOL,L,M,1",
            // Density / Molarity → MolWeight
            "G_ML,MM,G_PER_MOL,1000000",
            "G_ML,M,G_PER_MOL,1000",
            // Density / MolWeight → Molarity
            "G_ML,G_PER_MOL,M,1000",
    })
    void testDivideSuccess(@ConvertWith(UnitConverter.class) MeasurementUnit unitA, @ConvertWith(UnitConverter.class) MeasurementUnit unitB, @ConvertWith(UnitConverter.class) MeasurementUnit expectedUnit, double expectedValue) {
        EnteredValue<MeasurementUnit> valueA = userEntered("1.0", unitA, 1);
        EnteredValue<MeasurementUnit> valueB = userEntered("1.0", unitB, 1);
        EnteredValue<MeasurementUnit> result = EnteredValue.divide(valueA, valueB);
        assertThat(result).hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testDivideInconvertibleUnitsThrows() {
        EnteredValue<WeightUnit> weight = userEntered("1.0", G, 1);
        EnteredValue<MolarityUnit> volume = userEntered("1.0", MolarityUnit.MM, 1);
        Assertions.assertThatThrownBy(() -> EnteredValue.divide(weight, volume))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot divide units: G and MM");
    }

    static class UnitConverter implements ArgumentConverter {

        @Override
        public Object convert(@Nullable Object source, ParameterContext context) throws ArgumentConversionException {
            MeasurementUnit unit = UNIT_NAMES.get((String) source);
            checkArgument(unit != null, "Unknown unit: %s", source);
            return unit;
        }
    }
}
