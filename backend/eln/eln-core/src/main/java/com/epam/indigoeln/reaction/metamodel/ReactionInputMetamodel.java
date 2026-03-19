package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.*;
import com.epam.indigoeln.reaction.model.patch.handler2.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.*;

public class ReactionInputMetamodel {

    // ReactionRow
    public static final ModelProperty<ReactionInput, CompoundRef, ReactionInputPatch, CompoundRefPatch> COMPOUND = property("compound", ReactionRow::getCompound, ReactionRow::setCompound, AbstractReactionRowPatch::getCompound, AbstractReactionRowPatch::setCompound, CompoundRefDiffHandler.INSTANCE);
    public static final ModelProperty<ReactionInput, EnteredValue<NoUnit>, ReactionInputPatch, EnteredValuePatch<NoUnit>> EQ = enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, AbstractReactionRowPatch::getEq, AbstractReactionRowPatch::setEq, new EnteredValueDiffHandler<>(null, EnteredValue.DEFAULT_ONE));
    // ReactionInput
    public static final ModelProperty<ReactionInput, InputAnchor, ReactionInputPatch, InputAnchor> ANCHOR = property("anchor", ReactionInput::getAnchor, ReactionInput::setAnchor, ReactionInputPatch::getAnchor, ReactionInputPatch::setAnchor);
    public static final ModelProperty<ReactionInput, ReactionRole, ReactionInputPatch, ReactionRole> ROLE = property("role", ReactionInput::getRole, ReactionInput::setRole, ReactionInputPatch::getRole, ReactionInputPatch::setRole);
    public static final ModelProperty<ReactionInput, EnteredValue<MolUnit>, ReactionInputPatch, EnteredValuePatch<MolUnit>> MOL = enteredValueProperty("mol", ReactionInput::getMol, ReactionInput::setMol, ReactionInputPatch::getMol, ReactionInputPatch::setMol);
    public static final ModelProperty<ReactionInput, String, ReactionInputPatch, String> CHEMICAL_NAME = property("chemicalName", ReactionInput::getChemicalName, ReactionInput::setChemicalName, ReactionInputPatch::getChemicalName, ReactionInputPatch::setChemicalName);
    public static final ModelProperty<ReactionInput, Boolean, ReactionInputPatch, Boolean> LIMITING = property("limiting", ReactionInput::isLimiting, ReactionInput::setLimiting, ReactionInputPatch::getLimiting, ReactionInputPatch::setLimiting, DefaultDiffHandler.DEFAULT_FALSE_INSTANCE);
    public static final ModelProperty<ReactionInput, List<ReactionInputSample>, ReactionInputPatch, ListPatch<ReactionInputSample, ReactionInputSamplePatch>> SAMPLES = listProperty("samples", ReactionInput::getSamples, ReactionInput::setSamples, ReactionInputPatch::getSamples, ReactionInputPatch::setSamples, new ListDiffHandler<>(ReactionInputSample::getAnchor, new MetamodelDiffHandler<>(ReactionInputSampleMetamodel.INSTANCE, ReactionInputSamplePatch::new)));

    public static final Metamodel<ReactionInput, ReactionInputPatch> INSTANCE = new Metamodel<>("ReactionInput", List.of(
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
