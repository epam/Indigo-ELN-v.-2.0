package com.epam.indigoeln.reaction.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SignificantFiguresUtilTest {

    @ParameterizedTest
    @CsvSource({
            // normal rounding
            "123.456,  3, 123",
            "123.456,  4, 123.5",
            "123.456,  6, 123.456",
            "0.001234, 2, 0.0012",
            "0.001235, 2, 0.0012",
            "9.9995,   4, 10.00",
            // negative numbers
            "-123.456, 3, -123",
            "-0.001234,2, -0.0012",
            // single significant figure
            "123.456,  1, 100",
            "0.056,    1, 0.06",
            // zero
            "0.0,      3, 0",
            // large numbers
            "1234567.89, 4, 1235000",
            // special values
            "NaN,      3, NaN",
            "Infinity,     3, Infinity"
    })
    void testFormatToSignificantFigures(double value, int significantFigures, String expected) {
        assertThat(SignificantFiguresUtil.formatToSignificantFigures(value, significantFigures)).isEqualTo(expected);
    }
}
