package com.epam.indigoeln.core.util;

public class EqualsUtil {

    private static double PRECISION = 0.00000001;

    public static boolean doubleEqZero(double number){
        return Math.abs(number - 0.0) < PRECISION ? true: false;
    }

    public static boolean doubleNumEq(double num1, double num2){
        return Math.abs(num1 - num2) < PRECISION ? true: false;
    }
}
