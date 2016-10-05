package com.epam.indigoeln.core.service.sd;

import java.util.Map;

public class SDExportItem {

    private String molfile;

    private Map<String, String> properties;

    public String getMolfile() {
        return molfile;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
