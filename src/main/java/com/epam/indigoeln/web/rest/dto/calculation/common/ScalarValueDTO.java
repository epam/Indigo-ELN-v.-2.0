package com.epam.indigoeln.web.rest.dto.calculation.common;

public class ScalarValueDTO {

    private double value;
    private boolean entered;
    private boolean readonly;

    public ScalarValueDTO() {
    }

    public ScalarValueDTO(double value, boolean entered, boolean readonly) {
        this.value = value;
        this.entered = entered;
        this.readonly = readonly;
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

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
