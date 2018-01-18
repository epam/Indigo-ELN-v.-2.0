package com.epam.indigoeln.core.util;

/**
 * Utility class for equals check.
 */
public final class EqualsUtil {

    private static final double PRECISION = 0.00000001;

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

}
