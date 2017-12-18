package com.epam.indigoeln.core.util;

/**
 * Utility class for equals check.
 */
public final class EqualsUtil {

    public static final double PRECISION = 0.00000001;

    private EqualsUtil() {
    }

    /**
     * Returns true if number equals zero with preset accuracy.
     *
     * @param number Number for check
     * @return True if number equals zero with preset accuracy, otherwise returns false
     */
    public static boolean doubleEqZero(double number) {
        return Math.abs(number - 0.0) < PRECISION;
    }

    /**
     * Returns true if difference between two numbers less than preset accuracy.
     *
     * @param num1 Number
     * @param num2 Number
     * @return True if difference between two numbers less than preset accuracy, otherwise returns false
     */
    public static boolean doubleNumEq(double num1, double num2) {
        return Math.abs(num1 - num2) < PRECISION;
    }
}