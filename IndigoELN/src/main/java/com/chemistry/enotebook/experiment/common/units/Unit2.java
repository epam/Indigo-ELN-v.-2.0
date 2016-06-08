package com.chemistry.enotebook.experiment.common.units;

import com.chemistry.enotebook.experiment.common.GenericCode2;

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

    private Unit2(String code) {
        super();

        if (code == null) {
            code = UnitFactory2.createUnitOfType(type).getCode();
            setCode(code);
        } else if (!code.equals("--") && !code.equals("null")) {
            UnitCache2 uc = UnitCache2.getInstance();
            Unit2 unit = uc.getUnit(code);
            this.code = unit.getCode();
            this.description = unit.getDescription();
            this.type = unit.getType();
            this.displayValue = unit.getDisplayValue();
            this.stdCode = unit.getStdCode();
            this.stdConversionFactor = unit.getStdConversionFactor();
            this.stdDisplayFigs = unit.getStdDisplayFigs();
        }
        this.ordinal = nextOrdinal++;

    }

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

    public void finalize() {
        type = null;
    }

    /**
     * This is a kludge because of the necessity to set a single value upon load from storage. This needs to be revisited when
     * load/save happen in an easier manner.
     *
     * @param code The code to set - entire unit is rebuilt from UnitCache.getUnit(code)
     */
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

    public int compareTo(Unit2 o) {
        int comp = this.type.compareTo(o.getType());
        if (comp == 0) {
            comp = this.ordinal - o.ordinal;
        }
        return comp;
    }

    public String toString() {
        return this.displayValue;
    }

    public int hashCode() {
        return (HASH_PRIME + ordinal * HASH_PRIME) + this.type.hashCode();
    }

    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Unit2) {
            Unit2 test = (Unit2) obj;
            result = (type.equals(test.type) && code.equals(test.code));
        }
        return result;
    }

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

    public Unit2 deepClone() {
        return new Unit2(getCode(), this.getType(), this.getDisplayValue(), this.getDescription(),
                this.getStdCode(), this.getStdConversionFactor(), this.getStdDisplayFigs());
    }
}
