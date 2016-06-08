package com.chemistry.enotebook.experiment.utils;

import com.chemistry.enotebook.domain.AmountModel;

import java.util.List;

public class CeNNumberUtils {

    public static final int DEFAULT_SIG_DIGITS = 3;

    public static boolean doubleEquals(double x, double y, double delta) {
        boolean result;
        if (Math.abs(x) < Math.abs(y))
            result = (Math.abs(Math.abs(x) - Math.abs(y)) <= delta);
        else
            result = (Math.abs(Math.abs(y) - Math.abs(x)) <= delta);
        return result;
    }

    public static boolean doubleEquals(double x, double y) {
        double defaultDoubleDelta = 0.000000001;
        return doubleEquals(x, y, defaultDoubleDelta);
    }


    /**
     * Note on Significant Figures:
     * <p>
     * Online: http://chemed.chem.purdue.edu/genchem/topicreview/bp/ch1/sigfigs.html#determ 1) All nonzero digits are
     * significant--457 cm (three significant figures); 0.25 g (two significant figures). 2) Zeros between nonzero digits are
     * significant--1005 kg (four significant figures); 1.03 cm (three significant figures). 3) Zeros within a number are always
     * significant. Both 4308 and 40.05 contain four significant figures 4) Zeros to the left of the first nonzero digits in a
     * number are not significant; they merely indicate the position of the decimal point--0.02 g (one significant figure); 0.0026
     * cm (two significant figures). 5) When a number ends in zeros that are to the right of the decimal point, they are
     * significant--0.0200 g (three significant figures); 3.0 cm (two significant figures).
     * <p>
     * Scientist override on standard rules above. 1) Scientists do not want to deal with the "." when there are trailing zeros in a
     * number. Instead they wish to have the decimal point understood. Making "10" two sig figs and "1100" four sig figs.
     * <p>
     * Does not deal with negative values.
     *
     * @param val -
     *            number represented as a string from which we will extract s
     * @return String = representing the significant figures from the input string. No decimal is returned.
     */
    public static String getSigFigsFromNumberString(String val) {
        StringBuilder result = new StringBuilder();
        int indexOfDecimal = val.indexOf(".");
        // March through string to find nonzero and sig zero characters.
        for (int i = 0; i < val.length(); i++) {
            if (i != indexOfDecimal) {
                int number = Character.getNumericValue(val.charAt(i));
                // handle numbers like 123: item 1
                if (number > 0) {
                    result.append(number);
                } else if (result.length() > 0 && number == 0) {
                    // handle numbers like 1100
                    result.append(number);
                } else if (indexOfDecimal >= 0 && i > indexOfDecimal) {
                    // handle numbers with values after decimal point
                    if (result.length() > 0 && number == 0) {
                        result.append(number);
                    }
                }
            }
        }
        return result.toString();
    }

    public static int getSmallestSigFigsFromAmountModelList(List<AmountModel> amounts) {
        int smallestSigFig = 9999;
        for (AmountModel amt : amounts) {
            if (amt.getSigDigitsSet() && amt.getSigDigits() < smallestSigFig)
                smallestSigFig = amt.getSigDigits();
        }
        return (smallestSigFig == 9999 ? DEFAULT_SIG_DIGITS : smallestSigFig);
    }
}
