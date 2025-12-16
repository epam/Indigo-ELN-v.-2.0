package com.epam.indigoeln.reaction.util;

public class Flag {

    private boolean value = false;

    public void set() {
        value = true;
    }

    public boolean isSet() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
