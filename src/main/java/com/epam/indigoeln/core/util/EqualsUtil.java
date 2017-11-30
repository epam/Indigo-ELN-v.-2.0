package com.epam.indigoeln.core.util;

public final class EqualsUtil {

    public static final double PRECISION = 0.00000001;

    private EqualsUtil() {
    }

    public static boolean doubleEqZero(double number) {
        return Math.abs(number - 0.0) < PRECISION;
    }

    public static boolean doubleNumEq(double num1, double num2) {
        return Math.abs(num1 - num2) < PRECISION;
    }
}