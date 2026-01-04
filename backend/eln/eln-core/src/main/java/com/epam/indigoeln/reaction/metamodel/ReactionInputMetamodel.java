package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.patch.ReactionInputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionInputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.DefaultDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import org.jspecify.annotations.Nullable;

class ReactionInputMetamodel {

    public static final Metamodel<ReactionInput, ReactionInputPatch> INSTANCE = Metamodels.createMetamodel("ReactionInput", m -> {
        m.property("anchor", ReactionInput::getAnchor, ReactionInput::setAnchor, ReactionInputPatch::getAnchor, ReactionInputPatch::setAnchor);
        m.accept(Metamodels::buildReactionRowMetamodel);
        m.property("role", ReactionInput::getRole, ReactionInput::setRole, ReactionInputPatch::getRole, ReactionInputPatch::setRole);
        m.enteredValueProperty("mol", ReactionInput::getMol, ReactionInput::setMol, ReactionInputPatch::getMol, ReactionInputPatch::setMol);
        m.<@Nullable String>property("chemicalName", ReactionInput::getChemicalName, ReactionInput::setChemicalName, ReactionInputPatch::getChemicalName, ReactionInputPatch::setChemicalName);
        m.property("limiting", ReactionInput::isLimiting, ReactionInput::setLimiting, ReactionInputPatch::getLimiting, ReactionInputPatch::setLimiting, new DefaultDiffHandler<>(false));
        m.listProperty("samples", ReactionInput::getSamples, ReactionInput::setSamples, ReactionInputPatch::getSamples, ReactionInputPatch::setSamples, new ListDiffHandler<>(ReactionInputSample::getAnchor, new MetamodelDiffHandler<>(ReactionInputSampleMetamodel.INSTANCE, ReactionInputSamplePatch::new)));
    });
}
