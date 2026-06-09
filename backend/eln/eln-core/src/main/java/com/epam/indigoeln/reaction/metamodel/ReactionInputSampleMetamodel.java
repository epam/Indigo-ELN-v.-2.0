package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionSample;
import com.epam.indigoeln.reaction.model.units.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.enteredValueProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionInputSampleMetamodel {

    // ReactionSample
    public static final ModelProperty<ReactionInputSample, @Nullable EnteredValue<DensityUnit>> DENSITY = enteredValueProperty("density", ReactionSample::getDensity, ReactionSample::setDensity);
    public static final ModelProperty<ReactionInputSample, @Nullable EnteredValue<MolarityUnit>> MOLARITY = enteredValueProperty("molarity", ReactionSample::getMolarity, ReactionSample::setMolarity);
    public static final ModelProperty<ReactionInputSample, @Nullable EnteredValue<VolumeUnit>> VOLUME = enteredValueProperty("volume", ReactionSample::getVolume, ReactionSample::setVolume);
    public static final ModelProperty<ReactionInputSample, EnteredValue<NoUnit>> PURITY = enteredValueProperty("purity", ReactionSample::getPurity, ReactionSample::setPurity, EnteredValue.DEFAULT_ONE_HUNDRED);
    public static final ModelProperty<ReactionInputSample, @Nullable STRCodeSample> STR_CODE = property("strCode", ReactionSample::getStrCode, ReactionSample::setStrCode);
    public static final ModelProperty<ReactionInputSample, List<HealthHazardRef>> HEALTH_HAZARDS = property("healthHazards", ReactionSample::getHealthHazards, ReactionSample::setHealthHazards);
    // ReactionInputSample
    public static final ModelProperty<ReactionInputSample, InputSampleAnchor> ANCHOR = property("anchor", ReactionInputSample::getAnchor, null);
    public static final ModelProperty<ReactionInputSample, @Nullable UUID> SAMPLE_ID = property("sampleId", ReactionInputSample::getSampleId, ReactionInputSample::setSampleId);
    public static final ModelProperty<ReactionInputSample, @Nullable EnteredValue<MolUnit>> MOL = enteredValueProperty("mol", ReactionInputSample::getMol, ReactionInputSample::setMol);
    public static final ModelProperty<ReactionInputSample, @Nullable EnteredValue<WeightUnit>> WEIGHT = enteredValueProperty("weight", ReactionInputSample::getWeight, ReactionInputSample::setWeight);
    public static final ModelProperty<ReactionInputSample, @Nullable String> COMMENT = property("comment", ReactionInputSample::getComment, ReactionInputSample::setComment);

    public static final Metamodel<ReactionInputSample> INSTANCE = new Metamodel<>("ReactionInputSample", List.of(
            DENSITY,
            MOLARITY,
            VOLUME,
            PURITY,
            STR_CODE,
            HEALTH_HAZARDS,
            ANCHOR,
            SAMPLE_ID,
            MOL,
            WEIGHT,
            COMMENT
    ));
}
