package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionInputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.listProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionMetamodel {

    public static final ModelProperty<Reaction, ReactionAnchor, ReactionPatch, ReactionAnchor> ANCHOR = property("anchor",Reaction::getAnchor, Reaction::setAnchor, ReactionPatch::getAnchor, ReactionPatch::setAnchor);
    public static final ModelProperty<Reaction, String, ReactionPatch, String> RXN_FILE = property("rxnfile", Reaction::getRxnfile, Reaction::setRxnfile, ReactionPatch::getRxnfile, ReactionPatch::setRxnfile);
    public static final ModelProperty<Reaction, Integer, ReactionPatch, Integer> RXN_VERSION = property("rxnVersion", Reaction::getRxnVersion, Reaction::setRxnVersion, ReactionPatch::getRxnVersion, ReactionPatch::setRxnVersion);
    public static final ModelProperty<Reaction, List<ReactionInput>, ReactionPatch, ListPatch<ReactionInput, ReactionInputPatch>> INPUTS = listProperty("inputs", Reaction::getInputs, Reaction::setInputs, ReactionPatch::getInputs, ReactionPatch::setInputs, new ListDiffHandler<>(ReactionInput::getAnchor, new MetamodelDiffHandler<>(ReactionInputMetamodel.INSTANCE, ReactionInputPatch::new)));
    public static final ModelProperty<Reaction, List<ReactionOutput>, ReactionPatch, ListPatch<ReactionOutput, ReactionOutputPatch>> OUTPUTS = listProperty("outputs", Reaction::getOutputs, Reaction::setOutputs, ReactionPatch::getOutputs, ReactionPatch::setOutputs, new ListDiffHandler<>(ReactionOutput::getAnchor, new MetamodelDiffHandler<>(ReactionOutputMetamodel.INSTANCE, ReactionOutputPatch::new)));
    public static final ModelProperty<Reaction, List<STRCodeSample>, ReactionPatch, List<STRCodeSample>> PRECURSOR_REACTANT_IDS = property("precursorReactantIds", Reaction::getPrecursorReactantIds, null, ReactionPatch::getPrecursorReactantIds, ReactionPatch::setPrecursorReactantIds);

    public static final Metamodel<Reaction, ReactionPatch> INSTANCE = new Metamodel<>("Reaction", List.of(
            ANCHOR,
            RXN_FILE,
            RXN_VERSION,
            INPUTS,
            OUTPUTS,
            PRECURSOR_REACTANT_IDS
    ));
}
