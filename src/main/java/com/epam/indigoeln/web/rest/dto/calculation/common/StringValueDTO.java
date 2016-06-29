package com.epam.indigoeln.web.rest.dto.calculation.common;

public class StringValueDTO {

    private String name;
    private boolean entered;
    private boolean readonly;

    public StringValueDTO() {
    }

    public StringValueDTO(String name, boolean entered, boolean readonly) {
        this.name = name;
        this.entered = entered;
        this.readonly = readonly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
