package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public abstract class AbstractReactionSamplePatch<A extends Anchor> {

    @Nullable
    protected Patched<EnteredValue<DensityUnit>, EnteredValuePatch<DensityUnit>> density;

    @Nullable
    protected Patched<EnteredValue<MolarityUnit>, EnteredValuePatch<MolarityUnit>> molarity;

    @Nullable
    protected Patched<EnteredValue<VolumeUnit>, EnteredValuePatch<VolumeUnit>> volume;

    @Nullable
    protected Patched<EnteredValue<NoUnit>, EnteredValuePatch<NoUnit>> purity;

    @Nullable
    protected Patched<STRCodeSample, STRCodeSample> strCode;

    @Nullable
    protected Patched<List<DictionaryItemRef>, List<DictionaryItemRef>> healthHazards;
}
