package com.epam.indigoeln.web.rest.dto.calculation;

import lombok.Data;

import java.util.List;

@Data
public class ListContainsDTO {

    private List<String> structures;
    private String query;
}
