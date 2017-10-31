package com.epam.indigoeln.bingodb.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ErrorDTO implements Serializable {

    private String message;
}
