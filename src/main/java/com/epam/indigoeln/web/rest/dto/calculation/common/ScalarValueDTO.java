package com.epam.indigoeln.web.rest.dto.calculation.common;

public class ScalarValueDTO extends CommonValueDTO {

    private double value;

    public ScalarValueDTO() {
    }

    public ScalarValueDTO(double value, String displayValue, boolean entered, boolean readonly) {
        super(displayValue, entered, readonly);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
