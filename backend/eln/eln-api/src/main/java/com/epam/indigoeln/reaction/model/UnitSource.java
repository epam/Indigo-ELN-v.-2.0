package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;

import static com.epam.indigoeln.common.model.units.MolUnit.MOL;
import static com.epam.indigoeln.common.model.units.MolarityUnit.M;
import static com.epam.indigoeln.common.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;

@Data
@With
@RequiredArgsConstructor
public class UnitSource {

    public static final String CONTEXT_KEY = "unitSourceContext";

    public static final UnitSource DEFAULT_UNITS = new UnitSource(G, ML, MOL, M);

    private final WeightUnit weightUnit;
    private final VolumeUnit volumeUnit;
    private final MolUnit molUnit;
    private final MolarityUnit molarityUnit;
}
