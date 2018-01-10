package com.epam.indigoeln.web.rest.dto.search.request;

import lombok.Data;

@Data
public class ExperimentsSearchRequest {
    private final String experimentFullName;
    private int limit = 100;
}
