package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
public sealed abstract class ReactionSample implements ExperimentModelNode, ToStringTree permits ReactionInputSample, ReactionOutputSample {

    @Nullable
    protected EnteredValue<DensityUnit> density;

    @Nullable
    protected EnteredValue<MolarityUnit> molarity;

    @Nullable
    protected EnteredValue<VolumeUnit> volume;

    @NotNull
    protected EnteredValue<NoUnit> purity;

    @Nullable
    protected STRCodeSample strCode;

    @NotNull
    protected List<DictionaryItemRef> healthHazards = List.of();

    @Override
    public void prepareToRecalculate() {
        EnteredValue.prepareToRecalculate(density, this::setDensity);
        EnteredValue.prepareToRecalculate(molarity, this::setMolarity);
        EnteredValue.prepareToRecalculate(volume, this::setVolume);
        EnteredValue.prepareToRecalculate(purity, this::setPurity, EnteredValue.DEFAULT_ONE);
    }

    @Override
    public void collectDictionaries(Consumer<@Nullable DictionaryItemRef> consumer) {
        healthHazards.forEach(consumer);
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
