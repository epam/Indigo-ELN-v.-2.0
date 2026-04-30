package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.*;

public class ReactionInputMetamodel {

    // ReactionRow
    public static final ModelProperty<ReactionInput, CompoundRef> COMPOUND = property("compound", ReactionRow::getCompound, ReactionInput::updateCompound);
    public static final ModelProperty<ReactionInput, EnteredValue<NoUnit>> EQ = enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, EnteredValue.DEFAULT_ONE);
    public static final ModelProperty<ReactionInput, Integer> RXN_POSITION = property("rxnPosition", ReactionRow::getRxnPosition, ReactionRow::setRxnPosition);
    // ReactionInput
    public static final ModelProperty<ReactionInput, InputAnchor> ANCHOR = property("anchor", ReactionInput::getAnchor, ReactionInput::setAnchor);
    public static final ModelProperty<ReactionInput, ReactionRole> ROLE = property("role", ReactionInput::getRole, ReactionInput::setRole);
    public static final ModelProperty<ReactionInput, EnteredValue<MolUnit>> MOL = enteredValueProperty("mol", ReactionInput::getMol, ReactionInput::setMol);
    public static final ModelProperty<ReactionInput, String> CHEMICAL_NAME = property("chemicalName", ReactionInput::getChemicalName, ReactionInput::setChemicalName);
    public static final ModelProperty<ReactionInput, Boolean> LIMITING = property("limiting", ReactionInput::isLimiting, ReactionInput::setLimiting);
    public static final ModelProperty<ReactionInput, List<ReactionInputSample>> SAMPLES = listProperty("samples", ReactionInput::getSamples, ReactionInput::setSamples, ReactionInputSampleMetamodel.INSTANCE);

    public static final Metamodel<ReactionInput> INSTANCE = new Metamodel<>("ReactionInput", List.of(
            COMPOUND,
            EQ,
            ANCHOR,
            ROLE,
            MOL,
            CHEMICAL_NAME,
            LIMITING,
            SAMPLES
    ));
}
