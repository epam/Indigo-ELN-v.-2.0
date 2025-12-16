package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MolWeightUnit implements MeasurementUnit {

    G_PER_MOL(1.0);

    private final double multiplier;
}
