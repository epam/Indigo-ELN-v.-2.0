package com.epam.indigoeln.web.rest.dto.calculation.common;

public class CommonValueDTO {
    private String displayValue;
    private boolean entered;
    private boolean readonly;

    public CommonValueDTO() {
    }

    public CommonValueDTO(String displayValue, boolean entered, boolean readonly) {
        this.displayValue = displayValue;
        this.entered = entered;
        this.readonly = readonly;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
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
