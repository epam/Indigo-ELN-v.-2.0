package com.epam.indigoeln.core.service.calculation.helper;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RendererResult implements Serializable {

    private static final long serialVersionUID = -6575893956964671094L;

    private byte[] image;

    public RendererResult(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }

    public void setImage(byte[] image) {
        this.image = Arrays.copyOf(image, image.length);
    }
}
