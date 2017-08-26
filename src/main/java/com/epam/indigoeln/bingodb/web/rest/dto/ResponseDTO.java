package com.epam.indigoeln.bingodb.web.rest.dto;

import com.epam.indigoeln.bingodb.domain.BingoStructure;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseDTO implements Serializable {

    private List<BingoStructure> structures;
}
