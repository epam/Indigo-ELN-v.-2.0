package com.epam.indigoeln.web.rest.dto.calculation.common;

public class UnitValueDTO extends CommonValueDTO {

    private double value;
    private String unit;

    public UnitValueDTO() {
    }

    public UnitValueDTO(double value, String displayValue, String unit, boolean entered, boolean readonly) {
        super(displayValue, entered, readonly);
        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
