package com.epam.indigoeln.reaction.model.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoUnit implements MeasurementUnit {

    NO_UNIT(1);

    private final double multiplier;
}
