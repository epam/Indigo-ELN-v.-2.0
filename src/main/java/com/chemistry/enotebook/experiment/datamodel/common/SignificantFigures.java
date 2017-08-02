package com.chemistry.enotebook.experiment.datamodel.common;

/**
 * A number with an associated number of significant figures. This class handles parsing numbers, determining the number of
 * significant figures, adjusting the number of significant figures (including scientific rounding), and displaying the number. More
 * information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/SignificantFigures.html">ostermiller.org</a>.
 * <p>
 * When parsing a number to determine the number of significant figures, these rules are used:
 * <ul>
 * <li>Non-zero digits are always significant.</li>
 * <li>All zeros between other significant digits are significant.</li>
 * <li>All zeros left of the decimal point between a significant digit and the decimal point are significant.</li>
 * <li>All trailing zeros to the right of the decimal point are significant.</li>
 * <li>If the number contains no digits other than zero, every zero is significant.</li>
 * </ul>
 * <p>
 * When rounding a number the following rules are used:
 * <ul>
 * <li>If the greatest insignificant digit is less than five, round down.</li>
 * <li>If the greatest insignificant digit is greater than five, round up.</li>
 * <li>If the greatest insignificant digit is five and followed by some non-zero digit, round up.</li>
 * <li>If the greatest insignificant digit is five and followed only by zeros, and the least significant digit is odd, round up.</li>
 * <li>If the greatest insignificant digit is five and followed only by zeros, and the least significant digit is even, round down.</li>
 * </ul>
 * <p>
 * <p>
 * Example of using this class to multiply numbers and display the result with the proper number of significant figures:<br>
 * <p>
 * <pre>
 *   String[] args = {&quot;1.0&quot;, &quot;2.0&quot;, ...}
 *   SignificantFigures number;
 *   int sigs = Integer.MAX_VALUE;
 *   double result = 1D;
 *   for (int i=0; i&lt;args.length; i++){
 *       number = new SignificantFigures(args[i]);
 *       sigs = Math.min(sigs, number.getNumberSignificantFigures());
 *       result *= number.doubleValue();
 *   }
 *   number = new SignificantFigures(result);
 *   number.setNumberSignificantFigures(sigs);
 *   //System.out.println(number);
 * </pre>
 * <p>
 * <p>
 * Example of using this class to add numbers and display the result with the proper number of significant figures:<br>
 * <p>
 * <pre>
 *   String[] args = {&quot;1.0&quot;, &quot;2.0&quot;, ...}
 *   SignificantFigures number;
 *   int lsd = Integer.MIN_VALUE;
 *   int msd = Integer.MIN_VALUE;
 *   double result = 0D;
 *   for (int i=0; i&lt;args.length; i++){
 *       number = new SignificantFigures(args[i]);
 *       lsd = Math.max(lsd, number.getLSD());
 *       msd = Math.max(msd, number.getMSD());
 *       result += number.doubleValue();
 *   }
 *   number = new SignificantFigures(result);
 *   number.setLMSD(lsd, msd);
 *   //System.out.println(number);
 * </pre>
 *
 * @since ostermillerutils 1.00.00
 */
public class SignificantFigures extends Number {

    private static final long serialVersionUID = -8255137381460981712L;
    /**
     * Parsing state: Initial state before anything read.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int INITIAL = 0;
    /**
     * Parsing state: State in which a possible sign and possible leading zeros have been read.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int LEADZEROS = 1;
    /**
     * Parsing state: State in which a possible sign and at least one non-zero digit has been read followed by some number of zeros.
     * The decimal place has no been encountered yet.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int MIDZEROS = 2;
    /**
     * Parsing state: State in which a possible sign and at least one non-zero digit has been read. The decimal place has no been
     * encountered yet.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int DIGITS = 3;
    /**
     * Parsing state: State in which only a possible sign, leading zeros, and a decimal point have been encountered.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int LEADZEROSDOT = 4;
    /**
     * Parsing state: State in which a possible sign, at least one nonzero digit and a decimal point have been encountered.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int DIGITSDOT = 5;
    /**
     * Parsing state: State in which the exponent symbol 'E' has been encountered.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int MANTISSA = 6;
    /**
     * Parsing state: State in which the exponent symbol 'E' has been encountered followed by a possible sign or some number of
     * digits.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int MANTISSADIGIT = 7;
    /**
     * In the case the a number could not be parsed, the original is stored for toString purposes.
     *
     * @since ostermillerutils 1.00.00
     */
    private String original;
    /**
     * Buffer of the significant digits.
     *
     * @since ostermillerutils 1.00.00
     */
    private StringBuilder digits;
    /**
     * The exponent of the digits if a decimal place were inserted after the first digit.
     *
     * @since ostermillerutils 1.00.00
     */
    private int mantissa = -1;
    /**
     * positive if true, negative if false.
     *
     * @since ostermillerutils 1.00.00
     */
    private boolean sign = true;
    /**
     * True if this number has no non-zero digits.
     *
     * @since ostermillerutils 1.00.00
     */
    private boolean isZero = false;

    /**
     * Create a SignificantFigures object from a String representation of a number.
     *
     * @param number String representation of the number.
     * @throws NumberFormatException if the String is not a valid number.
     * @since ostermillerutils 1.00.00
     */
    private SignificantFigures(String number) throws NumberFormatException {
        original = number;
        parse(original);
    }

    /**
     * Convenience method to display a number with the correct significant digits.
     *
     * @param number             the number to display
     * @param significantFigures the number of significant figures to display.
     * @throws NumberFormatException if the String is not a valid number.
     * @since ostermillerutils 1.02.07
     */
    public static String format(String number, int significantFigures) throws NumberFormatException {
        SignificantFigures sf = new SignificantFigures(number);
        sf.setNumberSignificantFigures(significantFigures);
        return sf.toString();
    }

    /**
     * Adjust the number of digits in the number. Pad the tail with zeros if too short, round the number according to scientific
     * rounding if too long, leave alone if just right.
     * <p>
     * This method has no effect if this number is not a number or infinity. If the value of digits is "0", then no sigfigs will be
     * applied.
     *
     * @param significantFigures desired number of significant figures.
     * @return This number.
     * @since ostermillerutils 1.00.00
     */
    private void setNumberSignificantFigures(int significantFigures) {
        if (significantFigures <= 0)
            throw new IllegalArgumentException("Desired number of significant figures must be positive.");
        if (digits != null && !"0".equals(digits.toString()) && !"0.0".equals(digits.toString())) {
            int length = digits.length();
            if (length < significantFigures) {
                // number is not long enough, pad it with zeros.
                for (int i = length; i < significantFigures; i++) {
                    digits.append('0');
                }
            } else if (length > significantFigures) {
                // number is too long chop some of it off with rounding.
                boolean addOne; // we need to round up if true.
                char firstInSig = digits.charAt(significantFigures);
                if (firstInSig < '5') {
                    // first non-significant digit less than five, round down.
                    addOne = false;
                } else if (firstInSig == '5') {
                    // first non-significant digit equal to five
                    addOne = false;
                    for (int i = significantFigures + 1; !addOne && i < length; i++) {
                        // if its followed by any non-zero digits, round up.
                        if (digits.charAt(i) != '0') {
                            addOne = true;
                        }
                    }
                    if (!addOne) {
                        // if it was not followed by non-zero digits
                        // if the last significant digit is odd round up
                        // if the last significant digit is even round down
                        addOne = (digits.charAt(significantFigures - 1) & 1) == 1;
                    }
                } else {
                    // first non-significant digit greater than five, round up.
                    addOne = true;
                }
                // loop to add one (and carry a one if added to a nine)
                // to the last significant digit
                for (int i = significantFigures - 1; addOne && i >= 0; i--) {
                    char digit = digits.charAt(i);
                    if (digit < '9') {
                        digits.setCharAt(i, (char) (digit + 1));
                        addOne = false;
                    } else {
                        digits.setCharAt(i, '0');
                    }
                }
                if (addOne) {
                    // if the number was all nines
                    digits.insert(0, '1');
                    mantissa++;
                }
                // chop it to the correct number of figures.
                digits.setLength(significantFigures);
            }
        }
    }

    /**
     * Formats this number. If the number is less than 10^-3 or greater than or equal to 10^7, or the number might have an ambiguous
     * number of significant figures, scientific notation will be used.
     * <p>
     * A string such as "NaN" or "Infinity" may be returned by this method.
     *
     * @return representation of this number.
     * @since ostermillerutils 1.00.00
     */
    public String toString() { //NOSONAR toString should convert to string properly
        if (digits == null)
            return original;
        StringBuilder sb = new StringBuilder(this.digits.toString());
        int length = sb.length();

        if (mantissa <= -9 || mantissa >= 9 || (isZero && mantissa != 0)) {
            // use scientific notation.
            if (length > 1) {
                sb.insert(1, '.');
            }
            if (mantissa != 0) {
                sb.append("E").append(mantissa);
            }
        } else if (mantissa <= -1) {
            sb.insert(0, "0.");
            for (int i = mantissa; i < -1; i++) {
                sb.insert(2, '0');
            }
        } else if (mantissa < length && !(mantissa == length - 1)) {
            sb.insert(mantissa + 1, '.');
        } else {
            for (int i = length; i <= mantissa; i++) {
                sb.append('0');
            }
        }
        if (!sign) {
            sb.insert(0, '-');
        }
        return sb.toString();
    }

    /**
     * Parse a number from the given string. A valid number has an optional sign, some digits with an optional decimal point, and an
     * optional scientific notation part consisting of an 'E' followed by an optional sign, followed by some digits.
     *
     * @param number String representation of a number.
     * @throws NumberFormatException if the string is not a valid number.
     * @since ostermillerutils 1.00.00
     */
    private void parse(String number) throws NumberFormatException {
        int length = number.length();
        digits = new StringBuilder(length);
        int state = INITIAL;
        int mantissaStart = -1;
        boolean foundMantissaDigit = false;
        // sometimes we don't know if a zero will be
        // significant or not when it is encountered.
        // keep track of the number of them so that
        // the all can be made significant if we find
        // out that they are.
        int zeroCount = 0;
        int leadZeroCount = 0;

        for (int i = 0; i < length; i++) {
            char c = number.charAt(i);
            switch (c) {
                case '.':
                    switch (state) {
                        case INITIAL:
                        case LEADZEROS:
                            state = LEADZEROSDOT;
                        break;
                        case MIDZEROS:
                            // we now know that these zeros
                            // are more than just trailing placeholders.
                            for (int j = 0; j < zeroCount; j++) {
                                digits.append('0');
                            }
                            zeroCount = 0;
                            state = DIGITSDOT;
                        break;
                        case DIGITS:
                            state = DIGITSDOT;
                        break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);

                    }
                break;
                case '+':
                    switch (state) {
                        case INITIAL:
                            sign = true;
                            state = LEADZEROS;
                        break;
                        case MANTISSA:
                            state = MANTISSADIGIT;
                        break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                    }
                break;
                case '-':
                    switch (state) {
                        case INITIAL:
                            sign = false;
                            state = LEADZEROS;
                        break;
                        case MANTISSA:
                            state = MANTISSADIGIT;
                        break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                    }
                break;
                case '0':
                    switch (state) {
                        case INITIAL:
                        case LEADZEROS:
                            // only significant if number
                            // is all zeros.
                            zeroCount++;
                            leadZeroCount++;
                            state = LEADZEROS;
                        break;
                        case MIDZEROS:
                        case DIGITS:
                            digits.append(c);
                            mantissa++;
                            state = DIGITS;
                        break;
                        case LEADZEROSDOT:
                            // only significant if number
                            // is all zeros.
                            mantissa--;
                            zeroCount++;
                            state = LEADZEROSDOT;
                        break;
                        case DIGITSDOT:
                            // non-leading zeros after
                            // a decimal point are always
                            // significant.
                            digits.append(c);
                        break;
                        case MANTISSA:
                        case MANTISSADIGIT:
                            foundMantissaDigit = true;
                            state = MANTISSADIGIT;
                        break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                    }
                break;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    switch (state) {
                        case INITIAL:
                        case LEADZEROS:
                        case DIGITS:
                            zeroCount = 0;
                            digits.append(c);
                            mantissa++;
                            state = DIGITS;
                        break;
                        case MIDZEROS:
                            // we now know that these zeros
                            // are more than just trailing placeholders.
                            for (int j = 0; j < zeroCount; j++) {
                                digits.append('0');
                            }
                            zeroCount = 0;
                            digits.append(c);
                            mantissa++;
                            state = DIGITS;
                        break;
                        case LEADZEROSDOT:
                        case DIGITSDOT:
                            zeroCount = 0;
                            digits.append(c);
                            state = DIGITSDOT;
                        break;
                        case MANTISSA:
                        case MANTISSADIGIT:
                            state = MANTISSADIGIT;
                            foundMantissaDigit = true;
                        break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                    }
                break;
                case 'E':
                case 'e':
                    switch (state) {
                        case INITIAL:
                        case LEADZEROS:
                        case DIGITS:
                        case LEADZEROSDOT:
                        case DIGITSDOT:
                            // record the starting point of the mantissa
                            // so we can do a substring to get it back later
                            mantissaStart = i + 1;
                            state = MANTISSA;
                            break;
                        default:
                            throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                    }
                    break;
                default:
                    throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
            }
        }
        if (mantissaStart != -1) {
            // if we had found an 'E'
            if (!foundMantissaDigit) {
                // we didn't actually find a mantissa to go with.
                throw new NumberFormatException("No digits in mantissa.");
            }
            // parse the mantissa.
            mantissa += Integer.parseInt(number.substring(mantissaStart));
        }
        if (digits.length() == 0) {
            if (zeroCount > 0) {
                // if nothing but zeros all zeros are significant.
                for (int j = 0; j < zeroCount; j++) {
                    digits.append('0');
                }
                mantissa += leadZeroCount;
                isZero = true;
                sign = true;
            } else {
                // a hack to catch some cases that we could catch
                // by adding a ton of extra states. Things like:
                // "e2" "+e2" "+." "." "+" etc.
                throw new NumberFormatException("No digits in number.");
            }
        }
    }

    /**
     * Returns the value of this number as a byte.
     *
     * @return the numeric value represented by this object after conversion to type byte.
     * @throws NumberFormatException if this number cannot be converted to a byte.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public byte byteValue() throws NumberFormatException {
        return Byte.parseByte(original);
    }

    /**
     * Returns the value of this number as a double.
     *
     * @return the numeric value represented by this object after conversion to type double.
     * @throws NumberFormatException if this number cannot be converted to a double.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public double doubleValue() throws NumberFormatException {
        return Double.parseDouble(original);
    }

    /**
     * Returns the value of this number as a float.
     *
     * @return the numeric value represented by this object after conversion to type float.
     * @throws NumberFormatException if this number cannot be converted to a float.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public float floatValue() throws NumberFormatException {
        return Float.parseFloat(original);
    }

    /**
     * Returns the value of this number as a int.
     *
     * @return the numeric value represented by this object after conversion to type int.
     * @throws NumberFormatException if this number cannot be converted to a int.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public int intValue() throws NumberFormatException {
        return Integer.parseInt(original);
    }

    /**
     * Returns the value of this number as a long.
     *
     * @return the numeric value represented by this object after conversion to type long.
     * @throws NumberFormatException if this number cannot be converted to a long.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public long longValue() throws NumberFormatException {
        return Long.parseLong(original);
    }

    /**
     * Returns the value of this number as a short.
     *
     * @return the numeric value represented by this object after conversion to type short.
     * @throws NumberFormatException if this number cannot be converted to a short.
     * @since ostermillerutils 1.00.00
     */
    @Override
    public short shortValue() throws NumberFormatException {
        return Short.parseShort(original);
    }
}
