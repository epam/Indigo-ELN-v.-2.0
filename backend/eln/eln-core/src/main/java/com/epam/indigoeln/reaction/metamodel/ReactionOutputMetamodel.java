package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

class ReactionOutputMetamodel {

    public static final Metamodel<ReactionOutput, ReactionOutputPatch> INSTANCE = Metamodels.createMetamodel("ReactionOutput", m -> {
        m.property("anchor", ReactionOutput::getAnchor, ReactionOutput::setAnchor, ReactionOutputPatch::getAnchor, ReactionOutputPatch::setAnchor);
        m.accept(Metamodels::buildReactionRowMetamodel);
        m.property("outputName", ReactionOutput::getOutputName, ReactionOutput::setOutputName, ReactionOutputPatch::getOutputName, ReactionOutputPatch::setOutputName);
        m.property("chemicalName", ReactionOutput::getChemicalName, ReactionOutput::setChemicalName, ReactionOutputPatch::getChemicalName, ReactionOutputPatch::setChemicalName);
        m.property("type", ReactionOutput::getType, ReactionOutput::setType, ReactionOutputPatch::getType, ReactionOutputPatch::setType);
        m.property("intended", ReactionOutput::isIntended, ReactionOutput::setIntended, ReactionOutputPatch::getIntended, ReactionOutputPatch::setIntended);
        m.enteredValueProperty("theoMol", ReactionOutput::getTheoMol, ReactionOutput::setTheoMol, ReactionOutputPatch::getTheoMol, ReactionOutputPatch::setTheoMol);
        m.enteredValueProperty("theoWeight", ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight, ReactionOutputPatch::getTheoWeight, ReactionOutputPatch::setTheoWeight);
        m.listProperty("samples", ReactionOutput::getSamples, ReactionOutput::setSamples, ReactionOutputPatch::getSamples, ReactionOutputPatch::setSamples, new ListDiffHandler<>(ReactionOutputSample::getAnchor, new MetamodelDiffHandler<>(ReactionOutputSampleMetamodel.INSTANCE, ReactionOutputSamplePatch::new)));
    });
}
