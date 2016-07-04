package com.epam.indigoeln.web.rest.dto.calculation.common;

public class StringValueDTO extends CommonValueDTO {

    private String name;

    public StringValueDTO() {
    }

    public StringValueDTO(String name, String displayValue, boolean entered, boolean readonly) {
        super(displayValue, entered, readonly);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
