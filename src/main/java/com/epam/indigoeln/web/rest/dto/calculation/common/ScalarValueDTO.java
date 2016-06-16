package com.epam.indigoeln.web.rest.dto.calculation.common;

public class ScalarValueDTO {

    private double value;
    private boolean entered;

    public ScalarValueDTO() {
    }

    public ScalarValueDTO(double value, boolean entered) {
        this.value = value;
        this.entered = entered;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }
}
