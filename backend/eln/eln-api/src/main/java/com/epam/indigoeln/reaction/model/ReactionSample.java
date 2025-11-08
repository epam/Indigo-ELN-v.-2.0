package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.AbstractReactionSamplePatch;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public sealed abstract class ReactionSample implements ExperimentModelNode permits ReactionInputSample, ReactionOutputSample {

    protected static <C extends ReactionSample, A extends Anchor, P extends AbstractReactionSamplePatch<A>> void addBaseProperties(Metamodel<C, P> metamodel) {
        metamodel.enteredValueProperty("density", ReactionSample::getDensity, ReactionSample::setDensity, AbstractReactionSamplePatch::getDensity, AbstractReactionSamplePatch::setDensity);
        metamodel.enteredValueProperty("molarity", ReactionSample::getMolarity, ReactionSample::setMolarity, AbstractReactionSamplePatch::getMolarity, AbstractReactionSamplePatch::setMolarity);
        metamodel.enteredValueProperty("volume", ReactionSample::getVolume, ReactionSample::setVolume, AbstractReactionSamplePatch::getVolume, AbstractReactionSamplePatch::setVolume);
        metamodel.enteredValueProperty("purity", ReactionSample::getPurity, ReactionSample::setPurity, AbstractReactionSamplePatch::getPurity, AbstractReactionSamplePatch::setPurity);
        metamodel.simpleProperty("strCode", ReactionSample::getStrCode, ReactionSample::setStrCode, AbstractReactionSamplePatch::getStrCode, AbstractReactionSamplePatch::setStrCode);
        metamodel.dictionaryListProperty("healthHazards", ReactionSample::getHealthHazards, ReactionSample::setHealthHazards, AbstractReactionSamplePatch::getHealthHazards, AbstractReactionSamplePatch::setHealthHazards);
    }

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
}
