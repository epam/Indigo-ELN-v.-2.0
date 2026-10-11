package com.epam.indigoeln.common.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ModelUtilTest {

    @ParameterizedTest
    @CsvSource({
        "2.0, 2",
        "0.50, 0.5",
        "0.51, 0.51",
        "20, 20",
        "20.00, 20",
        "0.0, 0",
    })
    void stripTrailingZeros(String value, String expected) {
        assertThat(ModelUtil.stripTrailingZeros(new BigDecimal(value)).toString()).isEqualTo(expected);
    }
}
