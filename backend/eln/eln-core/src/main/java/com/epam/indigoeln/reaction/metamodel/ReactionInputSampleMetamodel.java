package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionInputSamplePatch;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

class ReactionInputSampleMetamodel {

    public static final Metamodel<ReactionInputSample, ReactionInputSamplePatch> INSTANCE = Metamodels.createMetamodel("ReactionInputSample", m -> {
        m.property("anchor", ReactionInputSample::getAnchor, ReactionInputSample::setAnchor, ReactionInputSamplePatch::getAnchor, ReactionInputSamplePatch::setAnchor);
        m.accept(Metamodels::buildReactionSampleMetamodel);
        m.<@Nullable UUID>property("sampleId", ReactionInputSample::getSampleId, ReactionInputSample::setSampleId, ReactionInputSamplePatch::getSampleId, ReactionInputSamplePatch::setSampleId);
        m.enteredValueProperty("mol", ReactionInputSample::getMol, ReactionInputSample::setMol, ReactionInputSamplePatch::getMol, ReactionInputSamplePatch::setMol);
        m.enteredValueProperty("weight", ReactionInputSample::getWeight, ReactionInputSample::setWeight, ReactionInputSamplePatch::getWeight, ReactionInputSamplePatch::setWeight);
        m.<@Nullable String>property("comment", ReactionInputSample::getComment, ReactionInputSample::setComment, ReactionInputSamplePatch::getComment, ReactionInputSamplePatch::setComment);
    });
}
