package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentSearchBatch {

    @Nullable
    private Double batchPurity;

    @Nullable
    private Double batchYield;
}
