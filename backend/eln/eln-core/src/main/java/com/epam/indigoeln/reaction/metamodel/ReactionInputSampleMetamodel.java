package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionSample;
import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.ReactionInputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.EnteredValueDiffHandler;
import com.epam.indigoeln.reaction.model.units.*;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.enteredValueProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionInputSampleMetamodel {

    // ReactionSample
    public static final ModelProperty<ReactionInputSample, EnteredValue<DensityUnit>, ReactionInputSamplePatch, EnteredValuePatch<DensityUnit>> DENSITY = enteredValueProperty("density", ReactionSample::getDensity, ReactionSample::setDensity, ReactionInputSamplePatch::getDensity, ReactionInputSamplePatch::setDensity);
    public static final ModelProperty<ReactionInputSample, EnteredValue<MolarityUnit>, ReactionInputSamplePatch, EnteredValuePatch<MolarityUnit>> MOLARITY = enteredValueProperty("molarity", ReactionSample::getMolarity, ReactionSample::setMolarity, ReactionInputSamplePatch::getMolarity, ReactionInputSamplePatch::setMolarity);
    public static final ModelProperty<ReactionInputSample, EnteredValue<VolumeUnit>, ReactionInputSamplePatch, EnteredValuePatch<VolumeUnit>> VOLUME = enteredValueProperty("volume", ReactionSample::getVolume, ReactionSample::setVolume, ReactionInputSamplePatch::getVolume, ReactionInputSamplePatch::setVolume);
    public static final ModelProperty<ReactionInputSample, EnteredValue<NoUnit>, ReactionInputSamplePatch, EnteredValuePatch<NoUnit>> PURITY = enteredValueProperty("purity", ReactionSample::getPurity, ReactionSample::setPurity, ReactionInputSamplePatch::getPurity, ReactionInputSamplePatch::setPurity, new EnteredValueDiffHandler<>(null, EnteredValue.DEFAULT_ONE_HUNDRED));
    public static final ModelProperty<ReactionInputSample, STRCodeSample, ReactionInputSamplePatch, STRCodeSample> STR_CODE = property("strCode", ReactionSample::getStrCode, ReactionSample::setStrCode, ReactionInputSamplePatch::getStrCode, ReactionInputSamplePatch::setStrCode);
    public static final ModelProperty<ReactionInputSample, List<DictionaryItemRef>, ReactionInputSamplePatch, List<DictionaryItemRef>> HEALTH_HAZARDS = property("healthHazards", ReactionSample::getHealthHazards, ReactionSample::setHealthHazards, ReactionInputSamplePatch::getHealthHazards, ReactionInputSamplePatch::setHealthHazards);
    // ReactionInputSample
    public static final ModelProperty<ReactionInputSample, InputSampleAnchor, ReactionInputSamplePatch, InputSampleAnchor> ANCHOR = property("anchor", ReactionInputSample::getAnchor, ReactionInputSample::setAnchor, ReactionInputSamplePatch::getAnchor, ReactionInputSamplePatch::setAnchor);
    public static final ModelProperty<ReactionInputSample, UUID, ReactionInputSamplePatch, UUID> SAMPLE_ID = property("sampleId", ReactionInputSample::getSampleId, ReactionInputSample::setSampleId, ReactionInputSamplePatch::getSampleId, ReactionInputSamplePatch::setSampleId);
    public static final ModelProperty<ReactionInputSample, EnteredValue<MolUnit>, ReactionInputSamplePatch, EnteredValuePatch<MolUnit>> MOL = enteredValueProperty("mol", ReactionInputSample::getMol, ReactionInputSample::setMol, ReactionInputSamplePatch::getMol, ReactionInputSamplePatch::setMol);
    public static final ModelProperty<ReactionInputSample, EnteredValue<WeightUnit>, ReactionInputSamplePatch, EnteredValuePatch<WeightUnit>> WEIGHT = enteredValueProperty("weight", ReactionInputSample::getWeight, ReactionInputSample::setWeight, ReactionInputSamplePatch::getWeight, ReactionInputSamplePatch::setWeight);
    public static final ModelProperty<ReactionInputSample, String, ReactionInputSamplePatch, String> COMMENT = property("comment", ReactionInputSample::getComment, ReactionInputSample::setComment, ReactionInputSamplePatch::getComment, ReactionInputSamplePatch::setComment);

    public static final Metamodel<ReactionInputSample, ReactionInputSamplePatch> INSTANCE = new Metamodel<>("ReactionInputSample", List.of(
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
