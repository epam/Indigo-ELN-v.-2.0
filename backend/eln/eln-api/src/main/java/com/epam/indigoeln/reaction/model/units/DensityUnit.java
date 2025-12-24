package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DensityUnit implements MeasurementUnit {

    G_ML(1.0);

    private final double multiplier;
}
