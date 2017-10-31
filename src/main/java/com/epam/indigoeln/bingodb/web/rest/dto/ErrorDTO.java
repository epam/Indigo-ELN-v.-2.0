package com.epam.indigoeln.bingodb.web.rest.dto;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class ErrorDTO implements Serializable {

    private String message;

    public ErrorDTO() {
        // Empty constructor for JSON deserialize
    }

    public ErrorDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
