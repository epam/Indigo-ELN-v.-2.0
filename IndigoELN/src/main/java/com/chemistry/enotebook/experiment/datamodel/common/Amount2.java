package com.chemistry.enotebook.experiment.datamodel.common;

import com.chemistry.enotebook.domain.CeNAbstractModel;
import com.chemistry.enotebook.experiment.common.interfaces.DeepClone;
import com.chemistry.enotebook.experiment.common.interfaces.DeepCopy;
import com.chemistry.enotebook.experiment.common.units.Unit2;
import com.chemistry.enotebook.experiment.common.units.UnitCache2;
import com.chemistry.enotebook.experiment.common.units.UnitFactory2;
import com.chemistry.enotebook.experiment.common.units.UnitType;
import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;


/**
 * Uses String type to store the value, so you should be able to store some pretty big digits. Uses BigDecimal when converting
 * values. Hence it doesn't lose its original sig figs. displayedFigs is set only when std unit display values need to be
 * overridden.
 * <p>
 * Note on Significant Figures:
 * <p>
 * Online: http://chemed.chem.purdue.edu/genchem/topicreview/bp/ch1/sigfigs.html#determ 1) All nonzero digits are significant--457
 * cm (three significant figures); 0.25 g (two significant figures). 2) Zeros between nonzero digits are significant--1005 kg (four
 * significant figures); 1.03 cm (three significant figures). 3) Zeros within a number are always significant. Both 4308 and 40.05
 * contain four significant figures 4) Zeros to the left of the first nonzero digits in a number are not significant; they merely
 * indicate the position of the decimal point--0.02 g (one significant figure); 0.0026 cm (two significant figures). 5) When a
 * number ends in zeros that are to the right of the decimal point, they are significant--0.0200 g (three significant figures); 3.0
 * cm (two significant figures).
 */
public class Amount2 extends CeNAbstractModel implements DeepClone, DeepCopy {
    /**
     *
     */
    private static final long serialVersionUID = 8301635693659209778L;
    private transient static final double DELTA_FIGS = 0.0000001;
    private transient final int MAX_FIGS = 10;
    private final transient int roundingPreference = BigDecimal.ROUND_HALF_UP;
    private StringBuffer value = new StringBuffer("0");
    // holds the defaultvalue
    private BigDecimal defaultValue = new BigDecimal("0");
    private Unit2 unit = UnitFactory2.createUnitOfType(UnitType.MASS);
    private boolean calculated = true;
    // need sense of maximum figs possible.
    private int userPrefFigs = -1; // Set by user entry. Overrides std unit figs to display
    private int displayedFigs = -1; // Amount of decimal places to show - overrides unit.getStdDisplayFigs()
    // Specifies if SignificantFigures count is set or not. If not set, the user prefs and such take over.
    private boolean sigDigitsSet = true;
    private int sigDigits = CeNNumberUtils.DEFAULT_SIG_DIGITS; // int holding the value's significant digits.
    // Specifies if the amount can be displayed
    private boolean isCanBeDisplayed = true;


    //////

    protected Amount2() {
    }

    public Amount2(UnitType unitType) {
        this();
        unit = UnitFactory2.createUnitOfType(unitType);
    }

    public Amount2(UnitType unitType, String defaultVal) {
        this();
        setDefaultValue(defaultVal);
        unit = UnitFactory2.createUnitOfType(unitType);
    }

    private Amount2(String val, Unit2 toUnit) {
        this();
        setValue(val);
        unit.deepCopy(toUnit);
    }

    public Amount2(String val, Unit2 toUnit, String defaultVal) {
        this(val, toUnit);
        setDefaultValue(defaultVal);
    }

    private Amount2(double val, Unit2 toUnit) {
        this();
        setValue(val);
        unit.deepCopy(toUnit);
    }

    public Amount2(double val, Unit2 toUnit, String defaultVal) {
        this(val, toUnit);
        setDefaultValue(defaultVal);
    }

    public Amount2(double val, Unit2 toUnit, String defaultVal, int sigDigs) {
        this(val, toUnit);
        setSigDigits(sigDigits);
        setDefaultValue(defaultVal);
    }

    public Amount2(UnitType unitType, double val, String defaultVal) {
        this();
        setDefaultValue(defaultVal);
        unit = UnitFactory2.createUnitOfType(unitType);
        setValue(val);
    }

    public Amount2(UnitType unitType, double val) {
        this();
        unit = UnitFactory2.createUnitOfType(unitType);
        setValue(val);
    }


    public Amount2(Amount2 amt) {
        deepCopy(amt);
    }

    public Amount2(UnitType unitType, double val, boolean isCalculated) {
        this();
        unit = UnitFactory2.createUnitOfType(unitType);
        setValue(val);
        setCalculated(isCalculated);
//        this.calculated = isCalculated;
    }

    public void finalize() throws Throwable {
        super.finalize();
        unit = null;
        value = null;
    }

    /**
     * Returns the value set to defaultValue and sets unit to default value Calls Modified
     */
    public void reset() {
        unit.deepCopy(UnitCache2.getInstance().getUnit(unit.getStdCode()));
        softReset();
    }

    /**
     * Returns the value set to defaultValue sets calc'd to true and resets displayFigs but leaves the unit alone Calls Modified
     */
    public void softReset() {
        value.setLength(0);
        value.append(defaultValue.toString());
        calculated = true;
        sigDigits = CeNNumberUtils.DEFAULT_SIG_DIGITS;
        sigDigitsSet = true;
        displayedFigs = -1;
        setModelChanged(true);
    }

    /**
     * Returns the value as if the amount object were set to that unit. Does not change the value or unit in the amount object. Does
     * not change the sig figs of the value.
     *
     * Side-effect: value is set to new value and unit is updated.
     */
    private String convertValue(Unit2 toUnit) {
        String result;
        // need to check unit is of specific type.
        if (getUnitType().equals(toUnit.getType()) && !unit.equals(toUnit)) {
            // bring unit to standard units
            String baseUnit = unit.getStdCode();
            double convFactor = unit.getStdConversionFactor();
            double fullConvValue = Double.parseDouble(getValue()) * convFactor;
            if (toUnit.getCode().equals(baseUnit)) {
                value.setLength(0);
                result = trimRightJunk(fullConvValue + "");
                value.append(result);
            } else {
                double toConvFactor = toUnit.getStdConversionFactor();
                fullConvValue = fullConvValue / toConvFactor;

                value.setLength(0);
                result = trimRightJunk(fullConvValue + "");
                value.append(result);
            }
        } else {
            result = getValue();
        }
        return result;
    }

    /*
     * Trims '*.0' values generated by the double division or multiplication on 2 double or int values. else it would return the
     * same val
     */
    private String trimRightJunk(String val) {
        StringBuilder nonZeroValue = new StringBuilder();
        if (val.endsWith(".0")) {
            int dotIndex = val.indexOf('.');
            nonZeroValue.append(val.substring(0, dotIndex));
        } else {
            // Trims the additional decimal places greater than MAX_FIGS
            int index = val.indexOf('.');
            String fraction = val.substring(index, val.length());
            if (fraction.length() > 10) {
                nonZeroValue.append(SignificantFigures.format(val, MAX_FIGS));
            } else {
                nonZeroValue.append(val);
            }

        }
        return nonZeroValue.toString();
    }

    /**
     * Only valid after editing is finished.
     *
     * @return
     */
    public String getValue() {
        return value.toString();
        /*
		 * String result; if (sigDigitsSet && sigDigits > 0) { // Use this to allow for enteries like "60." to allow a setting of 2
		 * sig figs. result = SignificantFigures.format(value.toString(), sigDigits); // Use this to keep scientific notiation out
		 * of the display. if (result.indexOf("E") >= 0) result = new BigDecimal(SignificantFigures.format(value.toString(),
		 * sigDigits)).toString(); } else { // value is old school: pre sig figs. int decimalPlace = value.indexOf("."); int
		 * fixedFigs = getFixedFigs(); if (isCalculated() && decimalPlace >= 0 && value.substring(decimalPlace + 1,
		 * value.length()).length() > fixedFigs) result = new BigDecimal(value.toString()).setScale(fixedFigs,
		 * roundingPreference).toString(); else // user set. result = value.toString(); } return result;
		 */
    }

    /**
     * WARNING: using this will set the scale visible to users.
     */
    public void setValue(Double val) {
        if (!(val.isInfinite() && val.isNaN())) {
            BigDecimal bg = new BigDecimal(val);
            if (!isCalculated())
                setFixedFigs(bg.scale());
            setValue(bg);
        } else {
            setValue(defaultValue);
        }
    }

    public void setValue(BigDecimal val) {
        // value = new BigDecimal(val.unscaledValue(), val.scale()); // this is an effective copy of BigDecimal
        setValue(val.doubleValue()); // this is an effective copy of BigDecimal
        if (!isCalculated())
            setFixedFigs(val.scale());
        setModelChanged(true);
    }

    protected String GetValueForDisplay() {
        if (!isCanBeDisplayed) {
            return "";
        }

        String result; // May need to use this = value.toString();
        if (sigDigitsSet && sigDigits > 0) {
            // Use this to allow for enteries like "60." to allow a setting of 2 sig figs.
            result = SignificantFigures.format(value.toString(), sigDigits);
            // Use this to keep scientific notiation out of the display.
            // vb 11/20 remove - do all formatting in SignificantFigures
            /////if (result.indexOf("E") >= 0)
            /////	result = new BigDecimal(SignificantFigures.format(value.toString(), sigDigits)).toString();
        } else { // value is old school: pre sig figs.
            int decimalPlace = value.indexOf(".");
            int fixedFigs = getFixedFigs();
            if (isCalculated() && decimalPlace >= 0 && value.substring(decimalPlace + 1, value.length()).length() > fixedFigs)
                result = new BigDecimal(value.toString()).setScale(fixedFigs, roundingPreference).toString();
            else
                // user set.
                result = value.toString();
        }
        // //System.out.println("Value :"+ getValue());
        // //System.out.println("DisplayValue :"+ result);

        return result;
    }

    public double doubleValue() {
        return Double.parseDouble(getValue());
    }

    public double GetValueInStdUnitsAsDouble() {
        BigDecimal bd = new BigDecimal(getValue()).multiply(new BigDecimal(unit.getStdConversionFactor()));
        return bd.doubleValue();
    }

    /**
     * Sets the value of the Amount object to the value in numbers the string represents If the string is "" the default value is
     * used. The default value is initialized as 0.000;
     *
     * @param val -
     *            String representation of a number or "" or null;
     */
    public void setValue(String val) {
        setValue(val, isCalculated());
    }

    /**
     * Sets the value of the Amount object to the value in numbers the string represents If the string is "" the default value is
     * used. The default value is initialized as 0.000; isCalc indicates the status of the amount object whether it is a calculated
     * quantity or is a user set quantity.
     *
     * @param val -
     *            String representation of a number or "" or null;
     */
    public void setValue(String val, boolean isCalc) {
        if (StringUtils.isNotBlank(val)) {
            Double tstVal = new Double(val);
            if (!(tstVal.isInfinite() || tstVal.isNaN())) {
                value.setLength(0);
                if (tstVal != 0.0) {
                    // The value should be un-adulterated, so directly appending the val
                    // to value string buffer.
                    value.append(val);

                    // used to show historical values: values prior to application of sig digits update.
                    /*
					 * if (!sigDigitsSet || sigDigits <= 0) { if (val.indexOf(".") == 0) { value.append("0"); }
					 * ///System.out.println("Value 1--:"+value); value.append(val); ////System.out.println("Value 2--:"+value); }
					 * else {
					 *
					 * //System.out.println("Value 3--:"+value); value.append(SignificantFigures.format(val, sigDigits)).toString();
					 * //System.out.println("Value 4--:"+value); // value.append(new BigDecimal(SignificantFigures.format(val,
					 * sigDigits)).toString()); }
					 */
                } else {
                    value.append("0");
                }
                setCalculated(isCalc);
                setModelChanged(true);
            }
        } else {
            setValue(defaultValue, isCalc);
        }
    }

    public void setValue(double val) {
        setValue(Double.toString(val));
    }

    public void setValue(double val, boolean isCalc) {
        setValue(Double.toString(val), isCalc);
    }

    public void setValue(Double val, boolean isCalculated) {
        if (!(val.isInfinite() && val.isNaN())) {
            BigDecimal bg = new BigDecimal(val);
            setValue(bg, isCalculated);
        } else {
            setValue(defaultValue, true);
        }
    }

    public void setValue(BigDecimal val, boolean isCalc) {
        // Make sure there is no tie to the original value so that this one
        // will not change when the other is changed.
        setCalculated(isCalc);
        setValue(val);
    }

    public void SetValueInStdUnits(double val) {
        if (!(Double.isNaN(val) || Double.isInfinite(val))) {
            BigDecimal bd = new BigDecimal(val);
            setValue(bd.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
        } else {
            setValue(defaultValue.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
        }
    }

    public void SetValueInStdUnits(double val, boolean isCalc) {
        if (!(Double.isNaN(val) || Double.isInfinite(val))) {
            setCalculated(isCalc);
            // avoid going through the check for valid value twice
            BigDecimal bd = new BigDecimal(val / unit.getStdConversionFactor());
            // Get a grip on the proper number of sig figs to display.
            // If scale is 1/1000 then we will get a value of 0.001
            bd.setScale(getFixedFigs(), roundingPreference);
            setValue(bd);
        } else {
            setCalculated(true);
            setValue(defaultValue.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
        }
    }

    public Unit2 getUnit() {
        return unit;
    }

    public void setUnit(Unit2 toUnit) {
        // Invariant: Unit != null
        if (toUnit != null && !unit.equals(toUnit)) {
            // convert value to new unit.
            if (!getValue().trim().equals("") && Double.parseDouble(getValue().trim()) != 0.0) {
                String convVal = convertValue(toUnit);
                if (!sigDigitsSet)
                    setFixedFigs(getFixedFigsBasedOnString(convVal));
            }
            unit.deepCopy(toUnit);
        }
    }

    public UnitType getUnitType() {
        return unit.getType();
    }

    /**
     * Value the amount object will use when the value is set to "" or null.
     *
     * @return double
     */
    protected double getDefaultValue() {
        return defaultValue.doubleValue();
    }

    /**
     * Sets value that the amount object will use when created without a value, and used when a null string is entered in setValue
     * as the null string indicates the defaults are to be imposed.
     *
     * @param val -
     *            value to be used as default. Must be a number.
     */
    public void setDefaultValue(String val) {
        defaultValue = new BigDecimal(val);
    }

    /**
     * Sets value that the amount object will use when created without a value, and used when a null string is entered in setValue
     * as the null string indicates the defaults are to be imposed.
     *
     * @param val -
     *            value to be used as default.
     */
    public void setDefaultValue(double val) {
        defaultValue = new BigDecimal(val);
    }

    /**
     * Standard figures for display come from the unit itself. This can be overwritten by adding a user preference for figures to be
     * displayed Ultimately both are overridden should the user enter a value by hand. Then the Sig figs of that value are what are
     * important to display.
     *
     * @return int = figures after the decimal point to display
     */
    public int getFixedFigs() {
        int figsToDisplay = unit.getStdDisplayFigs();
        if (isCalculated()) {
            if (userPrefFigs >= 0)
                figsToDisplay = userPrefFigs;
            // else if (displayedFigs >= 0) figsToDisplay = displayedFigs;
        } else if (displayedFigs >= 0)
            figsToDisplay = displayedFigs;
        return figsToDisplay;
    }

    /**
     * Setting this value will cause it to override any default values.
     *
     * @param fixedFigs -
     *            number of places after the decimal to display
     */
    public void setFixedFigs(int fixedFigs) {
        displayedFigs = fixedFigs;
    }

    public int getUserPrefFigs() {
        return userPrefFigs;
    }

    /**
     *
     * @param fixedFigs -
     *            fixed number of places after decimal point to display figures.
     */
    public void setUserPrefFigs(int fixedFigs) {
        userPrefFigs = fixedFigs;
        if (fixedFigs > 0) {
            setSigDigits(-1);
        }
    }

    /**
     *
     * @param val
     *            the string representing the number.
     * @return number of digits behind the decimal point or 0.
     */
    private int getFixedFigsBasedOnString(String val) {
        int result = 0;
        if (val.indexOf(".") >= 0)
            result = val.length() - (val.indexOf(".") + 1);
        return result;
    }

    /**
     * Indicates whether or not the user entered this information is calculated ( = true) or entered by hand ( = false)
     *
     * @return false if user entered true if calculated
     */
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * Does trigger modified events
     *
     * @param calc =
     *            true if this amount is calculated or false if it was set by a user.
     */
    public void setCalculated(boolean calc) {
        if (calc != calculated) {
            calculated = calc;
            displayedFigs = -1;
            if (calc) {
                if (userPrefFigs >= 0)
                    displayedFigs = userPrefFigs;
            }
        }
    }

    /**
     * Use to determine if this amount value is set to its default value.
     *
     * @return boolean true for equals and false otherwise.
     */
    public boolean isValueDefault() {
        return CeNNumberUtils.doubleEquals(doubleValue(), getDefaultValue(), DELTA_FIGS);
    }

    private BigDecimal getBigDecimal(String value, int scale) {
        BigDecimal result = new BigDecimal(value);
        result = result.setScale(scale, roundingPreference);
        return result;
    }
    //////


    /**
     * Do not use this constructor, unless you only want default setup: UnitType.MASS
     */
//    public Amount2() {
//    }
//
//    public Amount2(UnitType unitType) {
//        this();
//        unit = UnitFactory2.createUnitOfType(unitType);
//    }
//
//    public Amount2(UnitType unitType, String defaultVal) {
//        this();
//        setDefaultValue(defaultVal);
//        unit = UnitFactory2.createUnitOfType(unitType);
//    }
//
//    public Amount2(double val, Unit2 toUnit) {
//        this();
//        setValue(val);
//        unit.deepCopy(toUnit);
//    }
//
//    public Amount2(UnitType unitType, double val, String defaultVal) {
//        this();
//        setDefaultValue(defaultVal);
//        unit = UnitFactory2.createUnitOfType(unitType);
//        setValue(val);
//    }
//
//    public Amount2(UnitType unitType, double val) {
//        this();
//        unit = UnitFactory2.createUnitOfType(unitType);
//        setValue(val);
//    }
//
//    public Amount2(UnitType unitType, double val, boolean isCalculated) {
//        this();
//        unit = UnitFactory2.createUnitOfType(unitType);
//        setValue(val);
//        setCalculated(isCalculated);
//    }
//
//    public void finalize() throws Throwable {
//        super.finalize();
//        unit = null;
//        value = null;
//    }
//
//    /**
//     * Returns the value set to defaultValue and sets unit to default value Calls Modified
//     */
//    public void reset() {
//        unit.deepCopy(UnitCache2.getInstance().getUnit(unit.getStdCode()));
//        softReset();
//    }
//
//    /**
//     * Returns the value set to defaultValue sets calc'd to true and resets displayFigs but leaves the unit alone Calls Modified
//     */
//    public void softReset() {
//        value.setLength(0);
//        value.append(defaultValue.toString());
//        calculated = true;
//        sigDigits = CeNNumberUtils.DEFAULT_SIG_DIGITS;
//        sigDigitsSet = true;
//        displayedFigs = -1;
//        setModelChanged(true);
//    }
//
//    /**
//     * Returns the value as if the amount object were set to that unit. Does not change the value or unit in the amount object. Does
//     * not change the sig figs of the value.
//     * <p>
//     * Side-effect: value is set to new value and unit is updated.
//     */
//    private String convertValue(Unit2 toUnit) {
//        String result;
//        // need to check unit is of specific type.
//        if (getUnitType().equals(toUnit.getType()) && !unit.equals(toUnit)) {
//            // bring unit to standard units
//            String baseUnit = unit.getStdCode();
//            double convFactor = unit.getStdConversionFactor();
//            double fullConvValue = Double.parseDouble(getValue()) * convFactor;
//            if (toUnit.getCode().equals(baseUnit)) {
//                value.setLength(0);
//                result = trimRightJunk(fullConvValue + "");
//                value.append(result);
//            } else {
//                double toConvFactor = toUnit.getStdConversionFactor();
//                fullConvValue = fullConvValue / toConvFactor;
//
//                value.setLength(0);
//                result = trimRightJunk(fullConvValue + "");
//                value.append(result);
//            }
//        } else {
//            result = getValue();
//        }
//        return result;
//    }
//
//    /*
//     * Trims '*.0' values generated by the double division or multiplication on 2 double or int values. else it would return the
//     * same val
//     */
//    private String trimRightJunk(String val) {
//        StringBuilder nonZeroValue = new StringBuilder();
//        if (val.endsWith(".0")) {
//            int dotIndex = val.indexOf('.');
//            nonZeroValue.append(val.substring(0, dotIndex));
//        } else {
//            // Trims the additional decimal places greater than MAX_FIGS
//            int index = val.indexOf('.');
//            String fraction = val.substring(index, val.length());
//            if (fraction.length() > 10) {
//                nonZeroValue.append(SignificantFigures.format(val, MAX_FIGS));
//            } else {
//                nonZeroValue.append(val);
//            }
//
//        }
//        return nonZeroValue.toString();
//    }
//
//    public String getValue() {
//        return value.toString();
//        /*
//         * String result; if (sigDigitsSet && sigDigits > 0) { // Use this to allow for enteries like "60." to allow a setting of 2
//		 * sig figs. result = SignificantFigures.format(value.toString(), sigDigits); // Use this to keep scientific notiation out
//		 * of the display. if (result.indexOf("E") >= 0) result = new BigDecimal(SignificantFigures.format(value.toString(),
//		 * sigDigits)).toString(); } else { // value is old school: pre sig figs. int decimalPlace = value.indexOf("."); int
//		 * fixedFigs = getFixedFigs(); if (isCalculated() && decimalPlace >= 0 && value.substring(decimalPlace + 1,
//		 * value.length()).length() > fixedFigs) result = new BigDecimal(value.toString()).setScale(fixedFigs,
//		 * roundingPreference).toString(); else // user set. result = value.toString(); } return result;
//		 */
//    }
//
//    private void setValue(BigDecimal val) {
//        setValue(val.doubleValue()); // this is an effective copy of BigDecimal
//        if (!isCalculated())
//            setFixedFigs(val.scale());
//        setModelChanged(true);
//    }
//
//    /**
//     * Sets the value of the Amount object to the value in numbers the string represents If the string is "" the default value is
//     * used. The default value is initialized as 0.000;
//     *
//     * @param val -
//     *            String representation of a number or "" or null;
//     */
//    public void setValue(String val) {
//        setValue(val, isCalculated());
//    }
//
//    public void setValue(double val) {
//        setValue(Double.toString(val));
//    }
//
//    protected String GetValueForDisplay() {
//        if (!isCanBeDisplayed) {
//            return "";
//        }
//
//        String result; // May need to use this = value.toString();
//        if (sigDigitsSet && sigDigits > 0) {
//            // Use this to allow for enteries like "60." to allow a setting of 2 sig figs.
//            result = SignificantFigures.format(value.toString(), sigDigits);
//            // Use this to keep scientific notiation out of the display.
//            // vb 11/20 remove - do all formatting in SignificantFigures
//        } else { // value is old school: pre sig figs.
//            int decimalPlace = value.indexOf(".");
//            int fixedFigs = getFixedFigs();
//            if (isCalculated() && decimalPlace >= 0 && value.substring(decimalPlace + 1, value.length()).length() > fixedFigs)
//                result = new BigDecimal(value.toString()).setScale(fixedFigs, roundingPreference).toString();
//            else
//                result = value.toString();
//        }
//
//        return result;
//    }
//
//    public double doubleValue() {
//        return Double.parseDouble(getValue());
//    }
//
//    public double GetValueInStdUnitsAsDouble() {
//        BigDecimal bd = new BigDecimal(getValue()).multiply(new BigDecimal(unit.getStdConversionFactor()));
//        return bd.doubleValue();
//    }
//
//    /**
//     * Sets the value of the Amount object to the value in numbers the string represents If the string is "" the default value is
//     * used. The default value is initialized as 0.000; isCalc indicates the status of the amount object whether it is a calculated
//     * quantity or is a user set quantity.
//     *
//     * @param val -
//     *            String representation of a number or "" or null;
//     */
//    public void setValue(String val, boolean isCalc) {
//        if (StringUtils.isNotBlank(val)) {
//            Double tstVal = new Double(val);
//            if (!(tstVal.isInfinite() || tstVal.isNaN())) {
//                value.setLength(0);
//                if (tstVal != 0.0) {
//                    // The value should be un-adulterated, so directly appending the val
//                    // to value string buffer.
//                    value.append(val);
//                } else {
//                    value.append("0");
//                }
//                setCalculated(isCalc);
//                setModelChanged(true);
//            }
//        } else {
//            setValue(defaultValue, isCalc);
//        }
//    }
//
//    public void setValue(double val, boolean isCalc) {
//        setValue(Double.toString(val), isCalc);
//    }
//
//    private void setValue(BigDecimal val, boolean isCalc) {
//        // Make sure there is no tie to the original value so that this one
//        // will not change when the other is changed.
//        setCalculated(isCalc);
//        setValue(val);
//    }
//
//    public void SetValueInStdUnits(double val) {
//        if (!(Double.isNaN(val) || Double.isInfinite(val))) {
//            BigDecimal bd = new BigDecimal(val);
//            setValue(bd.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
//        } else {
//            setValue(defaultValue.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
//        }
//    }
//
//    public void SetValueInStdUnits(double val, boolean isCalc) {
//        if (!(Double.isNaN(val) || Double.isInfinite(val))) {
//            setCalculated(isCalc);
//            // avoid going through the check for valid value twice
//            BigDecimal bd = new BigDecimal(val / unit.getStdConversionFactor());
//            // Get a grip on the proper number of sig figs to display.
//            // If scale is 1/1000 then we will get a value of 0.001
//            bd.setScale(getFixedFigs(), roundingPreference);
//            setValue(bd);
//        } else {
//            setCalculated(true);
//            setValue(defaultValue.divide(new BigDecimal(unit.getStdConversionFactor()), roundingPreference));
//        }
//    }
//
//    public Unit2 getUnit() {
//        return unit;
//    }
//
//    public void setUnit(Unit2 toUnit) {
//        // Invariant: Unit != null
//        if (toUnit != null && !unit.equals(toUnit)) {
//            // convert value to new unit.
//            if (!getValue().trim().equals("") && Double.parseDouble(getValue().trim()) != 0.0) {
//                String convVal = convertValue(toUnit);
//                if (!sigDigitsSet)
//                    setFixedFigs(getFixedFigsBasedOnString(convVal));
//            }
//            unit.deepCopy(toUnit);
//        }
//    }
//
//    public UnitType getUnitType() {
//        return unit.getType();
//    }
//
//    /**
//     * Value the amount object will use when the value is set to "" or null.
//     *
//     * @return double
//     */
//    protected double getDefaultValue() {
//        return defaultValue.doubleValue();
//    }
//
//    /**
//     * Sets value that the amount object will use when created without a value, and used when a null string is entered in setValue
//     * as the null string indicates the defaults are to be imposed.
//     *
//     * @param val -
//     *            value to be used as default.
//     */
//    protected void setDefaultValue(double val) {
//        defaultValue = new BigDecimal(val);
//    }
//
//    /**
//     * Sets value that the amount object will use when created without a value, and used when a null string is entered in setValue
//     * as the null string indicates the defaults are to be imposed.
//     *
//     * @param val -
//     *            value to be used as default. Must be a number.
//     */
//    public void setDefaultValue(String val) {
//        defaultValue = new BigDecimal(val);
//    }
//
//    /**
//     * Standard figures for display come from the unit itself. This can be overwritten by adding a user preference for figures to be
//     * displayed Ultimately both are overridden should the user enter a value by hand. Then the Sig figs of that value are what are
//     * important to display.
//     *
//     * @return int = figures after the decimal point to display
//     */
//    private int getFixedFigs() {
//        int figsToDisplay = unit.getStdDisplayFigs();
//        if (isCalculated()) {
//            if (userPrefFigs >= 0)
//                figsToDisplay = userPrefFigs;
//            // else if (displayedFigs >= 0) figsToDisplay = displayedFigs;
//        } else if (displayedFigs >= 0)
//            figsToDisplay = displayedFigs;
//        return figsToDisplay;
//    }
//
//    /**
//     * Setting this value will cause it to override any default values.
//     *
//     * @param fixedFigs -
//     *                  number of places after the decimal to display
//     */
//    private void setFixedFigs(int fixedFigs) {
//        displayedFigs = fixedFigs;
//    }
//
//    protected int getUserPrefFigs() {
//        return userPrefFigs;
//    }
//
//    /**
//     * @param fixedFigs -
//     *                  fixed number of places after decimal point to display figures.
//     */
//    protected void setUserPrefFigs(int fixedFigs) {
//        userPrefFigs = fixedFigs;
//        if (fixedFigs > 0) {
//            setSigDigits(-1);
//        }
//    }
//
//    /**
//     * @param val the string representing the number.
//     * @return number of digits behind the decimal point or 0.
//     */
//    private int getFixedFigsBasedOnString(String val) {
//        int result = 0;
//        if (val.contains("."))
//            result = val.length() - (val.indexOf(".") + 1);
//        return result;
//    }
//
//    /**
//     * Indicates whether or not the user entered this information is calculated ( = true) or entered by hand ( = false)
//     *
//     * @return false if user entered true if calculated
//     */
//    public boolean isCalculated() {
//        return calculated;
//    }
//
//    /**
//     * Does trigger modified events
//     *
//     * @param calc =
//     *             true if this amount is calculated or false if it was set by a user.
//     */
//    public void setCalculated(boolean calc) {
//        if (calc != calculated) {
//            calculated = calc;
//            displayedFigs = -1;
//            if (calc) {
//                if (userPrefFigs >= 0)
//                    displayedFigs = userPrefFigs;
//            }
//        }
//    }
//
//    /**
//     * Use to determine if this amount value is set to its default value.
//     *
//     * @return boolean true for equals and false otherwise.
//     */
//    public boolean isValueDefault() {
//        return CeNNumberUtils.doubleEquals(doubleValue(), getDefaultValue(), DELTA_FIGS);
//    }

    public String toString() {
        String result = getValue();
        if (isCalculated())
            result = GetValueForDisplay();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Amount2 amount2 = (Amount2) o;

        if (calculated != amount2.calculated) return false;
        if (userPrefFigs != amount2.userPrefFigs) return false;
        if (displayedFigs != amount2.displayedFigs) return false;
        if (roundingPreference != amount2.roundingPreference) return false;
        if (sigDigitsSet != amount2.sigDigitsSet) return false;
        if (sigDigits != amount2.sigDigits) return false;
        if (isCanBeDisplayed != amount2.isCanBeDisplayed) return false;
        if (value != null ? !value.equals(amount2.value) : amount2.value != null) return false;
        if (defaultValue != null ? !defaultValue.equals(amount2.defaultValue) : amount2.defaultValue != null)
            return false;
        return unit != null ? unit.equals(amount2.unit) : amount2.unit == null;

    }

    @Override
    public int hashCode() {
        int result = MAX_FIGS;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (calculated ? 1 : 0);
        result = 31 * result + userPrefFigs;
        result = 31 * result + displayedFigs;
        result = 31 * result + roundingPreference;
        result = 31 * result + (sigDigitsSet ? 1 : 0);
        result = 31 * result + sigDigits;
        result = 31 * result + (isCanBeDisplayed ? 1 : 0);
        return result;
    }

    public void deepCopy(Object source) {
        if (source instanceof Amount2) {
            Amount2 src = (Amount2) source;
            value.setLength(0);
            value.append(src.value.toString());
            defaultValue = new BigDecimal(src.defaultValue.toString());
            displayedFigs = src.displayedFigs;
            sigDigitsSet = src.sigDigitsSet;
            sigDigits = src.sigDigits;
            userPrefFigs = src.userPrefFigs;
            calculated = src.calculated;
            unit.deepCopy(src.unit);
        }
    }

    public Object deepClone() {
        Amount2 target = new Amount2(unit.getType());
        target.deepCopy(this);
        return target;
    }

    /**
     * @return Returns the sigDigits.
     */
    public int getSigDigits() {
        return sigDigits;
    }

    /**
     * @param sigDigits The sigDigits to set.
     */
    public void setSigDigits(int sigDigits) {
        this.sigDigits = sigDigits;
        sigDigitsSet = sigDigits > 0;
    }

    public boolean getSigDigitsSet() {
        return sigDigitsSet;
    }

    protected void setSigDigitsSet(boolean isSigDigitsSet) {
        this.sigDigitsSet = isSigDigitsSet;
    }

    protected int getDisplayedFigs() {
        return displayedFigs;
    }

    protected void setDisplayedFigs(int displayedFigs) {
        this.displayedFigs = displayedFigs;
    }

    protected boolean isCanBeDisplayed() {
        return isCanBeDisplayed;
    }

    protected void setCanBeDisplayed(boolean isCanBeDisplayed) {
        this.isCanBeDisplayed = isCanBeDisplayed;
    }

}
