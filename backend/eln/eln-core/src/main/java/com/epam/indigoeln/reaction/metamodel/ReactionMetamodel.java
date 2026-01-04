package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.patch.ReactionInputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

class ReactionMetamodel {

    public static final Metamodel<Reaction, ReactionPatch> INSTANCE = Metamodels.createMetamodel("Reaction", m -> {
        m.<Anchor.Reaction>property("anchor", Reaction::getAnchor, Reaction::setAnchor, ReactionPatch::getAnchor, ReactionPatch::setAnchor);
        m.property("rxnfile", Reaction::getRxnfile, Reaction::setRxnfile, ReactionPatch::getRxnfile, ReactionPatch::setRxnfile);
        m.property("rxnVersion", Reaction::getRxnVersion, Reaction::setRxnVersion, ReactionPatch::getRxnVersion, ReactionPatch::setRxnVersion);
        m.listProperty("inputs", Reaction::getInputs, Reaction::setInputs, ReactionPatch::getInputs, ReactionPatch::setInputs, new ListDiffHandler<>(ReactionInput::getAnchor, new MetamodelDiffHandler<>(ReactionInputMetamodel.INSTANCE, ReactionInputPatch::new)));
        m.listProperty("outputs", Reaction::getOutputs, Reaction::setOutputs, ReactionPatch::getOutputs, ReactionPatch::setOutputs, new ListDiffHandler<>(ReactionOutput::getAnchor, new MetamodelDiffHandler<>(ReactionOutputMetamodel.INSTANCE, ReactionOutputPatch::new)));
    });
}
