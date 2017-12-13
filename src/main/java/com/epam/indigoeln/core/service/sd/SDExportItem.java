package com.epam.indigoeln.core.service.sd;

import java.util.Map;

/**
 * SDFile item representation.
 */
public class SDExportItem {

    /**
     * Structure in MolFile format.
     */
    private String molfile;

    /**
     * Structure properties.
     */
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