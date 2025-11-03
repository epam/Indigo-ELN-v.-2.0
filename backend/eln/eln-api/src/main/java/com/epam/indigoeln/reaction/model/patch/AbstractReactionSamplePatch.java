package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.units.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractReactionSamplePatch<A extends Anchor> extends AbstractListElementPatch<A> {

    @Nullable
    protected Optional<EnteredValuePatch<DensityUnit>> density;

    @Nullable
    protected Optional<EnteredValuePatch<MolarityUnit>> molarity;

    @Nullable
    protected Optional<EnteredValuePatch<VolumeUnit>> volume;

    @Nullable
    protected Optional<EnteredValuePatch<NoUnit>> purity;

    @Nullable
    protected Optional<STRCodeSample> strCode;

    @Nullable
    protected Optional<List<DictionaryItemRef>> healthHazards;
}
