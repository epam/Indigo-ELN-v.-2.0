package com.epam.indigoeln.reaction.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SignificantFiguresUtil {

    public static final int MOL_WEIGHT_DECIMAL_PLACES = 2;

    private static final ThreadLocal<Integer> SIGNIFICANT_FIGURES = new ThreadLocal<>();

    public static int getSignificantFigures() {
        return SIGNIFICANT_FIGURES.get();
    }

    public static void setSignificantFigures(int significantFigures) {
        SIGNIFICANT_FIGURES.set(significantFigures);
    }

    public static void clearSignificantFigures() {
        SIGNIFICANT_FIGURES.remove();
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
