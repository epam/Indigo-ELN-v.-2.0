package com.epam.indigoeln.eln.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class GlobalSearchResultDTO extends BaseDTO {

    private EntityType type;
    private String name;
}
