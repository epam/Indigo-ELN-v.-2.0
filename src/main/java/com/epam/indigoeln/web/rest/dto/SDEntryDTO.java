package com.epam.indigoeln.web.rest.dto;

import java.util.Map;

public class SDEntryDTO {

    private String mol;

    private Map<String, String> properties;

    public String getMol() {
        return mol;
    }

    public void setMol(String mol) {
        this.mol = mol;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
