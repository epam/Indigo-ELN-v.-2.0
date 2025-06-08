package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
public abstract class ReactionSample implements ExperimentModelNode, ToStringTree {

    @Nullable
    protected EnteredValue<DensityUnit> density;

    @Nullable
    protected EnteredValue<MolarityUnit> molarity;

    @Nullable
    protected EnteredValue<VolumeUnit> volume;

    @NotNull
    protected EnteredValue<NoUnit> purity;

    @Override
    public void prepareToRecalculate() {
        EnteredValue.prepareToRecalculate(density, this::setDensity);
        EnteredValue.prepareToRecalculate(molarity, this::setMolarity);
        EnteredValue.prepareToRecalculate(volume, this::setVolume);
        EnteredValue.prepareToRecalculate(purity, this::setPurity, EnteredValue.DEFAULT_ONE);
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
