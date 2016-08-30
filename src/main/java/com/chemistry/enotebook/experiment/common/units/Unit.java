package com.chemistry.enotebook.experiment.common.units;

import com.chemistry.enotebook.experiment.common.GenericCode;

/**
 *
 *
 *
 */
public class Unit extends GenericCode implements Comparable {

    private static final long serialVersionUID = -1688969122894961308L;

    private static final int HASH_PRIME = 19471;
    private static int nextOrdinal = 0;
    private final int ordinal;
    private UnitType type;
    private String displayValue = "";
    private String stdCode = "--";
    private double stdConversionFactor = -1;
    private int stdDisplayFigs = 3;

    private Unit(String code) {
        super();
        setCode(code);
        this.ordinal = nextOrdinal++;
    }

    Unit(String code, UnitType type, String displayValue, String description, String stdCode, double stdConversionFactor,
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
    private void setCode(String code) {
        deepCopy(UnitCache.getInstance().getUnit(code));
    }

    /**
     * @return Returns the displayValue.
     */
    String getDisplayValue() {
        return displayValue;
    }

    public UnitType getType() {
        return type;
    }

    public int compareTo(Object o) {
        int comp = this.hashCode() - o.hashCode();
        if (o instanceof Unit) {
            comp = this.type.compareTo(((Unit) o).getType());
            if (comp == 0)
                comp = this.ordinal - ((Unit) o).ordinal;
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
        if (obj instanceof Unit) {
            Unit test = (Unit) obj;
            result = (type.equals(test.type) && code.equals(test.code));
        }
        return result;
    }

    public void deepCopy(Object source) {
        if (source instanceof Unit) {
            Unit src = (Unit) source;
            super.deepCopy(source);
            type = src.type;
            displayValue = src.displayValue;
            stdCode = src.stdCode;
            stdConversionFactor = src.stdConversionFactor;
            stdDisplayFigs = src.stdDisplayFigs;
        }
    }

    public Object deepClone() {
        return new Unit(getCode());
    }

}
