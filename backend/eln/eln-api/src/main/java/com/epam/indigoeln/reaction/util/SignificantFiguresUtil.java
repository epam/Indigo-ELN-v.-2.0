package com.epam.indigoeln.reaction.util;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SignificantFiguresUtil {

    public static final int MOL_WEIGHT_DECIMAL_PLACES = 2;

    private static final ScopedValue<Integer> SIGNIFICANT_FIGURES = ScopedValue.newInstance();

    public static int getSignificantFigures() {
        return SIGNIFICANT_FIGURES.get();
    }

    public static void runWithSignificantFigures(int significantFigures, Runnable action) {
        ScopedValue.where(SIGNIFICANT_FIGURES, significantFigures).run(action);
    }

    public static <T extends @Nullable Object, X extends Throwable> T callWithSignificantFigures(int significantFigures, ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(SIGNIFICANT_FIGURES, significantFigures).call(action);
    }

    public static String formatToSignificantFigures(double value) {
        return formatToSignificantFigures(value, SIGNIFICANT_FIGURES.get());
    }

    public static double roundToSignificantFigures(double value, int significantFigures) {
        return BigDecimal.valueOf(value).round(new MathContext(significantFigures, RoundingMode.HALF_UP)).doubleValue();
    }

    public static BigDecimal roundToDecimalPlaces(double value, int decimalPlaces) {
        return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    public static String formatToSignificantFigures(double value, int significantFigures) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Double.toString(value);
        }
        if (value == 0.0) {
            return "0";
        }
        BigDecimal bd = BigDecimal.valueOf(value).round(new MathContext(significantFigures, RoundingMode.HALF_UP));
        return bd.toPlainString();
    }
}
