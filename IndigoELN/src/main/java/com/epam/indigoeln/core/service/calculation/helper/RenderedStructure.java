package com.epam.indigoeln.core.service.calculation.helper;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RenderedStructure implements Serializable {

    private static final long serialVersionUID = -6575893956964671094L;

    private String structure;
    private byte[] image;

    public RenderedStructure(String structure, byte[] image) {
        setStructure(structure);
        setImage(image);
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
