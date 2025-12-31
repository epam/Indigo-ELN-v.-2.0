package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public abstract class AbstractReactionSamplePatch<A extends Anchor> {

    @Nullable
    protected Patched<EnteredValuePatch<DensityUnit>> density;

    @Nullable
    protected Patched<EnteredValuePatch<MolarityUnit>> molarity;

    @Nullable
    protected Patched<EnteredValuePatch<VolumeUnit>> volume;

    @Nullable
    protected Patched<EnteredValuePatch<NoUnit>> purity;

    @Nullable
    protected Patched<STRCodeSample> strCode;

    @Nullable
    protected Patched<List<DictionaryItemRef>> healthHazards;
}
