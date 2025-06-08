package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TotalCounts {

    @NotNull
    private Integer projects;

    @NotNull
    private Integer notebooks;

    @NotNull
    private Integer experiments;

    @NotNull
    private Map<ExperimentStatus, Integer> experimentsByStatus;
}
