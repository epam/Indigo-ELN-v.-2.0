package com.epam.indigoeln.core.model;

public class MolecularWeight {

    private String value;

    private boolean entered = false;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }
}
