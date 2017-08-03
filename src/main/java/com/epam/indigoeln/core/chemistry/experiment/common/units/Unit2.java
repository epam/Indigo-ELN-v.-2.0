package com.epam.indigoeln.core.chemistry.experiment.common.units;

import com.epam.indigoeln.core.chemistry.experiment.common.GenericCode2;

public class Unit2 extends GenericCode2 implements Comparable<Unit2> {
    public static final long serialVersionUID = 7526472295622776147L;
    private static final int HASH_PRIME = 19471;
    private static int nextOrdinal = 0;
    private final int ordinal;
    private UnitType type;
    private String displayValue = "";
    private String stdCode = "--";
    private double stdConversionFactor = -1;
    private int stdDisplayFigs = 3;

    Unit2(String code, UnitType type, String displayValue, String description, String stdCode, double stdConversionFactor,
          int stdDisplayFigs) {
        super();
        this.code = code;
        this.type = type;
        this.displayValue = displayValue;
        this.description = description;
        this.stdCode = stdCode;
        this.stdConversionFactor = stdConversionFactor;
        this.stdDisplayFigs = stdDisplayFigs;
        this.ordinal = nextOrdinal++;
    }

    /**
     * This is a kludge because of the necessity to set a single value upon load from storage. This needs to be revisited when
     * load/save happen in an easier manner.
     *
     * @param code The code to set - entire unit is rebuilt from UnitCache.getUnit(code)
     */
    @Override
    public void setCode(String code) {
        deepCopy(UnitCache2.getInstance().getUnit(code));
    }

    /**
     * @return Returns the displayValue.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * @return Returns the stdCode.
     */
    public String getStdCode() {
        return stdCode;
    }

    /**
     * @return Returns the stdConversionFactor.
     */
    public double getStdConversionFactor() {
        return stdConversionFactor;
    }

    /**
     * @return Returns the type.
     */
    public UnitType getType() {
        return type;
    }

    /**
     * Used to indicate what the default value for displaying figures after the decimal point should be for this unit type and code.
     */
    public int getStdDisplayFigs() {
        return stdDisplayFigs;
    }

    @Override
    public int compareTo(Unit2 o) {
        int comp = this.type.compareTo(o.getType());
        if (comp == 0) {
            comp = this.ordinal - o.ordinal;
        }
        return comp;
    }

    @Override
    public String toString() {
        return this.displayValue;
    }

    @Override
    public int hashCode() {
        return (HASH_PRIME + ordinal * HASH_PRIME) + this.type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Unit2) {
            Unit2 test = (Unit2) obj;
            result = (type.equals(test.type) && code.equals(test.code));
        }
        return result;
    }

    @Override
    public void deepCopy(Object source) {
        if (source instanceof Unit2) {
            Unit2 src = (Unit2) source;
            super.deepCopy(source);
            type = src.type;
            displayValue = src.displayValue;
            stdCode = src.stdCode;
            stdConversionFactor = src.stdConversionFactor;
            stdDisplayFigs = src.stdDisplayFigs;
        }
    }

    @Override
    public Unit2 deepClone() {
        return new Unit2(getCode(), this.getType(), this.getDisplayValue(), this.getDescription(),
                this.getStdCode(), this.getStdConversionFactor(), this.getStdDisplayFigs());
    }
}
