package com.epam.indigoeln.web.rest.dto.calculation.common;

public class StringValueDTO {

    private String name;
    private boolean entered;

    public StringValueDTO() {
    }

    public StringValueDTO(String name, boolean entered) {
        this.name = name;
        this.entered = entered;
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
}
