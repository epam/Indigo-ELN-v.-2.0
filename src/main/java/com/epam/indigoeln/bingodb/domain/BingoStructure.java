package com.epam.indigoeln.bingodb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class BingoStructure implements Serializable {

    private String id;
    private String structure;
}
