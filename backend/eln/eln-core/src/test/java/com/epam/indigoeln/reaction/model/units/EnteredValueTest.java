package com.epam.indigoeln.reaction.model.units;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.UnitSource;
import com.epam.indigoeln.reaction.service.calculator.Calculation;
import com.epam.indigoeln.reaction.util.CalculationContext;
import com.epam.indigoeln.reaction.util.UnitUtils;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.List;

import static com.epam.indigoeln.common.model.units.MolUnit.MOL;
import static com.epam.indigoeln.common.model.units.MolWeightUnit.G_PER_MOL;
import static com.epam.indigoeln.common.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.common.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;
import static com.epam.indigoeln.common.model.units.WeightUnit.MG;
import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.reaction.model.EnteredValue.userEntered;
import static com.google.common.base.Preconditions.checkArgument;

class EnteredValueTest {

    @RegisterExtension
    static final InvocationInterceptor SIG_FIGS_5 = new InvocationInterceptor() {
        @Override
        public void interceptTestMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> ctx, ExtensionContext ext) throws Throwable {
            CalculationContext.call(5, UnitSource.DEFAULT_UNITS, invocation::proceed);
        }
        @Override
        public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> ctx, ExtensionContext ext) throws Throwable {
            interceptTestMethod(invocation, ctx, ext);
        }
    };

    @ParameterizedTest
    @CsvSource({
            "1,G,2,G,3,G",
            "1,G,500,MG,1.5,G", // result is in base units (regardless of operand order)
            "500,MG,1,G,1.5,G",
            "0,G,1,MG,0.001,G",
    })
    void testAdd(String aValue, @ConvertWith(UnitConverter.class) WeightUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) WeightUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) WeightUnit expectedUnit) {
        EnteredValue<WeightUnit> result = value(aValue, aUnit).add(value(bValue, bUnit)).get();
        assertThat(result).isCalculated().hasValue(expectedValue, expectedUnit);
    }

    @ParameterizedTest
    @CsvSource({
            "3,G,1,G,2,G",
            "1,G,500,MG,0.5,G", // result is in base units (regardless of operand order)
            "1500,MG,1,G,0.5,G",
            "0,G,1,MG,-0.001,G",
    })
    void testSubtract(String aValue, @ConvertWith(UnitConverter.class) WeightUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) WeightUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) WeightUnit expectedUnit) {
        EnteredValue<WeightUnit> result = value(aValue, aUnit).subtract(value(bValue, bUnit)).get();
        assertThat(result).isCalculated().hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testAddInconvertibleUnitsThrows() {
        Calculation<MeasurementUnit> weight = value("1.0", G);
        Calculation<MeasurementUnit> volume = value("1.0", ML);
        Assertions.assertThatThrownBy(() -> weight.add(volume).get())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @Test
    void testSubtractInconvertibleUnitsThrows() {
        Calculation<MeasurementUnit> weight = value("1.0", G);
        Calculation<MeasurementUnit> volume = value("1.0", ML);
        Assertions.assertThatThrownBy(() -> weight.subtract(volume).get())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inconvertible units: G and ML");
    }

    @Test
    void testUserValueEqualityIncludesRevision() {
        Assertions.assertThat(userEntered("1", G, 1)).isEqualTo(userEntered("1", G, 1));
        Assertions.assertThat(userEntered("1", G, 1)).isNotEqualTo(userEntered("1", G, 2));
        Assertions.assertThat(userEntered("1", G, 1)).isNotEqualTo(userEntered("2", G, 1));
        Assertions.assertThat(EnteredValue.<WeightUnit>userEntered(null, null, 1)).isEqualTo(userEntered(null, null, 1));
        Assertions.assertThat(EnteredValue.<WeightUnit>userEntered(null, null, 1)).isNotEqualTo(userEntered(null, null, 2));
        Assertions.assertThat(EnteredValue.<WeightUnit>userEntered(null, null, 1)).isNotEqualTo(userEntered("1", G, 1));
    }

    @Test
    void testEmptyOperandGivesEmpty() {
        Calculation<WeightUnit> value = value("1", G);
        Calculation<WeightUnit> empty = Calculation.readOnly(EnteredValue.empty());
        assertThat(value.add(empty).get()).isEmpty();
        assertThat(empty.add(value).get()).isEmpty();
        assertThat(value.subtract(empty).get()).isEmpty();
        assertThat(empty.subtract(value).get()).isEmpty();
        assertThat(value.<WeightUnit>multiply(empty).get()).isEmpty();
        assertThat(empty.<WeightUnit>multiply(value).get()).isEmpty();
        assertThat(empty.multiply(2.0).get()).isEmpty();
        assertThat(value.<NoUnit>divide(empty).get()).isEmpty();
        assertThat(empty.<NoUnit>divide(value).get()).isEmpty();
        assertThat(value.percentageOf(empty).get()).isEmpty();
        assertThat(empty.percentageOf(value).get()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "2,MOL,3,G_PER_MOL,6,G",
            "2,MMOL,3,G_PER_MOL,0.006,G", // result is in base units
            "3,G_PER_MOL,2,MOL,6,G", // regardless of operand order
            "2,ML,3,M,0.006,MOL",
            "2,L,3,M,6,MOL",
            "2,ML,3,G_ML,6,G",
            "2,MG,3,NO_UNIT,0.006,G",
            "3,NO_UNIT,2,MG,0.006,G",
    })
    void testMultiply(String aValue, @ConvertWith(UnitConverter.class) MeasurementUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) MeasurementUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) MeasurementUnit expectedUnit) {
        EnteredValue<MeasurementUnit> result = value(aValue, aUnit).<MeasurementUnit>multiply(value(bValue, bUnit)).get();
        assertThat(result).isCalculated().hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testMultiplyInconvertibleUnitsThrows() {
        Calculation<WeightUnit> weight = value("1.0", G);
        Calculation<VolumeUnit> volume = value("1.0", ML);
        Assertions.assertThatThrownBy(() -> weight.multiply(volume).get())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot multiply units: G and ML");
    }

    @Test
    void testMultiplyScalar() {
        assertThat(value("2.0", G).multiply(3.0).get()).isCalculated().hasValue(6, G);
        assertThat(value("2.0", MG).multiply(3.0).get()).isCalculated().hasValue(6, MG); // keeps operand units
    }

    @ParameterizedTest
    @CsvSource({
            "6,G,3,G_PER_MOL,2,MOL",
            "6,G,2,MOL,3,G_PER_MOL",
            "6,MOL,2,ML,3000,M", // result is in units of the multiplication rule
            "6,G,3,G,2,NO_UNIT", // same base units
            "6,G,3,MG,2000,NO_UNIT",
            "6,G,3,NO_UNIT,2,G",
            "6,MG,3,NO_UNIT,0.002,G",
    })
    void testDivide(String aValue, @ConvertWith(UnitConverter.class) MeasurementUnit aUnit, String bValue, @ConvertWith(UnitConverter.class) MeasurementUnit bUnit, double expectedValue, @ConvertWith(UnitConverter.class) MeasurementUnit expectedUnit) {
        EnteredValue<MeasurementUnit> result = value(aValue, aUnit).<MeasurementUnit>divide(value(bValue, bUnit)).get();
        assertThat(result).isCalculated().hasValue(expectedValue, expectedUnit);
    }

    @Test
    void testDivideByZeroGivesEmpty() {
        assertThat(value("6", G).<MolUnit>divide(value("0", G_PER_MOL)).get()).isEmpty();
    }

    @Test
    void testDivideInconvertibleUnitsThrows() {
        Calculation<WeightUnit> weight = value("1.0", G);
        Calculation<MolarityUnit> molarity = value("1.0", MolarityUnit.MM);
        Assertions.assertThatThrownBy(() -> weight.divide(molarity).get())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot divide units: G and MM");
    }

    @ParameterizedTest
    @CsvSource({
            "1,G,4,G,25",
            "500,MG,2,G,25",
            "2,G,500,MG,400",
    })
    void testPercentageOf(String aValue, @ConvertWith(UnitConverter.class) WeightUnit aUnit, String totalValue, @ConvertWith(UnitConverter.class) WeightUnit totalUnit, double expectedValue) {
        EnteredValue<NoUnit> result = value(aValue, aUnit).percentageOf(value(totalValue, totalUnit)).get();
        assertThat(result).isCalculated().hasValue(expectedValue, NO_UNIT);
    }

    @Test
    void testSum() {
        assertThat(Calculation.sum(List.of(value("1", G), value("500", MG)), G).get()).isCalculated().hasValue(1.5, G);
        assertThat(Calculation.sum(List.<Calculation<WeightUnit>>of(), G).get()).isCalculated().hasValue(0, G);
    }

    @Test
    void testSumWithEmptyGivesEmpty() {
        List<Calculation<WeightUnit>> list = List.of(value("1", G), Calculation.readOnly(EnteredValue.empty()));
        assertThat(Calculation.sum(list, G).get()).isEmpty();
    }

    @Test
    void testSumInconvertibleUnitsThrows() {
        List<Calculation<MeasurementUnit>> list = List.of(value("1", G), value("1", ML));
        Assertions.assertThatThrownBy(() -> Calculation.sum(list, G).get())
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static <U extends MeasurementUnit> Calculation<U> value(String value, U unit) {
        return Calculation.readOnly(userEntered(value, unit, 1));
    }

    static class UnitConverter implements ArgumentConverter {

        @Override
        public Object convert(@Nullable Object source, ParameterContext context) throws ArgumentConversionException {
            MeasurementUnit unit = UnitUtils.ALL_UNITS.get((String) source);
            checkArgument(unit != null, "Unknown unit: %s", source);
            return unit;
        }
    }
}
