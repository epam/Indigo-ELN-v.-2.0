package com.epam.indigoeln.bingodb.web.rest.dto;

import java.io.Serializable;

public class ErrorDTO implements Serializable {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
