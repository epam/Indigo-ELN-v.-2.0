package com.epam.indigoeln.assay.service.calc;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class FormulaEvaluatorTest {

    private static final Map<String, double[]> INPUTS = Map.of(
            "x", new double[]{2},
            "y", new double[]{3},
            "controls", new double[]{10, 20, 30});

    private double eval(String expr) {
        return FormulaEvaluator.evaluate(expr, INPUTS);
    }

    @Test
    void respectsArithmeticPrecedenceAndParentheses() {
        assertThat(eval("2 + 3 * 4")).isEqualTo(14);
        assertThat(eval("(2 + 3) * 4")).isEqualTo(20);
        assertThat(eval("x + y")).isEqualTo(5);
        assertThat(eval("-x + 10")).isEqualTo(8);
    }

    @Test
    void exponentiationIsRightAssociative() {
        assertThat(eval("2^3^2")).isEqualTo(Math.pow(2, 9));
    }

    @Test
    void aggregatesVectorInputs() {
        assertThat(eval("mean(controls)")).isEqualTo(20);
        assertThat(eval("sd(controls)")).isEqualTo(10);
        assertThat(eval("max(controls) - min(controls)")).isEqualTo(20);
        assertThat(eval("count(controls)")).isEqualTo(3);
    }

    @Test
    void supportsScalarMathFunctions() {
        assertThat(eval("sqrt(abs(-16))")).isEqualTo(4);
        assertThat(eval("pow(2, 10)")).isEqualTo(1024);
        assertThat(eval("100 * (mean(controls) - x) / (mean(controls) - 0)"))
                .isCloseTo(90.0, within(1e-9));
    }

    @Test
    void rejectsMalformedOrUnsafeFormulas() {
        assertThatThrownBy(() -> eval("x +")).isInstanceOf(FormulaException.class);
        assertThatThrownBy(() -> eval("1 / 0")).isInstanceOf(FormulaException.class);
        assertThatThrownBy(() -> eval("x + controls")).isInstanceOf(FormulaException.class);
        assertThatThrownBy(() -> eval("bogus(x)")).isInstanceOf(FormulaException.class);
        assertThatThrownBy(() -> eval("unknownVar * 2")).isInstanceOf(FormulaException.class);
    }

    @Test
    void validatesSyntaxWithoutData() {
        FormulaEvaluator.validate("100 * (a - b) / (c - d)");
        assertThatThrownBy(() -> FormulaEvaluator.validate("100 * (a -"))
                .isInstanceOf(FormulaException.class);
    }
}
