package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.listProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionMetamodel {

    public static final ModelProperty<Reaction, ReactionAnchor> ANCHOR = property("anchor",Reaction::getAnchor, null);
    public static final ModelProperty<Reaction, @Nullable String> RXN_FILE = property("rxnfile", Reaction::getRxnfile, Reaction::setRxnfile);
    public static final ModelProperty<Reaction, List<ReactionInput>> INPUTS = listProperty("inputs", Reaction::getInputs, Reaction::setInputs, ReactionInputMetamodel.INSTANCE);
    public static final ModelProperty<Reaction, List<ReactionOutput>> OUTPUTS = listProperty("outputs", Reaction::getOutputs, Reaction::setOutputs, ReactionOutputMetamodel.INSTANCE);
    public static final ModelProperty<Reaction, List<STRCodeSample>> PRECURSOR_REACTANT_IDS = property("precursorReactantIds", Reaction::getPrecursorReactantIds, null);

    public static final Metamodel<Reaction> INSTANCE = new Metamodel<>("Reaction", List.of(
            ANCHOR,
            RXN_FILE,
            INPUTS,
            OUTPUTS,
            PRECURSOR_REACTANT_IDS
    ));
}
