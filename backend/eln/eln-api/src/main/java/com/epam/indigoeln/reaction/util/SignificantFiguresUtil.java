package com.epam.indigoeln.reaction.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SignificantFiguresUtil {

    public static final int MOL_WEIGHT_DECIMAL_PLACES = 2;

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
        // pad trailing zeros so result shows all significant figures (100 @ 5 -> 100.00)
        if (bd.precision() < significantFigures) {
            bd = bd.setScale(bd.scale() + significantFigures - bd.precision(), RoundingMode.UNNECESSARY);
        }
        return bd.toPlainString();
    }
}
