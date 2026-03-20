package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.*;
import com.epam.indigoeln.reaction.model.patch.handler2.CompoundRefDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.EnteredValueDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.*;

public class ReactionOutputMetamodel {

    // ReactionRow
    public static final ModelProperty<ReactionOutput, CompoundRef, ReactionOutputPatch, CompoundRefPatch> COMPOUND = property("compound", ReactionRow::getCompound, ReactionRow::setCompound, AbstractReactionRowPatch::getCompound, AbstractReactionRowPatch::setCompound, CompoundRefDiffHandler.INSTANCE);
    public static final ModelProperty<ReactionOutput, EnteredValue<NoUnit>, ReactionOutputPatch, EnteredValuePatch<NoUnit>> EQ = enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, AbstractReactionRowPatch::getEq, AbstractReactionRowPatch::setEq, new EnteredValueDiffHandler<>(null, EnteredValue.DEFAULT_ONE));
    // ReactionOutput
    public static final ModelProperty<ReactionOutput, OutputAnchor, ReactionOutputPatch, OutputAnchor> ANCHOR = property("anchor", ReactionOutput::getAnchor, ReactionOutput::setAnchor, ReactionOutputPatch::getAnchor, ReactionOutputPatch::setAnchor);
    public static final ModelProperty<ReactionOutput, String, ReactionOutputPatch, String> OUTPUT_NAME = property("outputName", ReactionOutput::getOutputName, ReactionOutput::setOutputName, ReactionOutputPatch::getOutputName, ReactionOutputPatch::setOutputName);
    public static final ModelProperty<ReactionOutput, String, ReactionOutputPatch, String> CHEMICAL_NAME = property("chemicalName", ReactionOutput::getChemicalName, ReactionOutput::setChemicalName, ReactionOutputPatch::getChemicalName, ReactionOutputPatch::setChemicalName);
    public static final ModelProperty<ReactionOutput, ReactionOutputType, ReactionOutputPatch, ReactionOutputType> TYPE = property("type", ReactionOutput::getType, ReactionOutput::setType, ReactionOutputPatch::getType, ReactionOutputPatch::setType);
    public static final ModelProperty<ReactionOutput, EnteredValue<MolUnit>, ReactionOutputPatch, EnteredValuePatch<MolUnit>> THEO_MOL = enteredValueProperty("theoMol", ReactionOutput::getTheoMol, ReactionOutput::setTheoMol, ReactionOutputPatch::getTheoMol, ReactionOutputPatch::setTheoMol);
    public static final ModelProperty<ReactionOutput, EnteredValue<WeightUnit>, ReactionOutputPatch, EnteredValuePatch<WeightUnit>> THEO_WEIGHT = enteredValueProperty("theoWeight", ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight, ReactionOutputPatch::getTheoWeight, ReactionOutputPatch::setTheoWeight);
    public static final ModelProperty<ReactionOutput, List<ReactionOutputSample>, ReactionOutputPatch, ListPatch<ReactionOutputSample, ReactionOutputSamplePatch>> SAMPLES = listProperty("samples", ReactionOutput::getSamples, ReactionOutput::setSamples, ReactionOutputPatch::getSamples, ReactionOutputPatch::setSamples, new ListDiffHandler<>(ReactionOutputSample::getAnchor, new MetamodelDiffHandler<>(ReactionOutputSampleMetamodel.INSTANCE, ReactionOutputSamplePatch::new)));

    public static final Metamodel<ReactionOutput, ReactionOutputPatch> INSTANCE = new Metamodel<>("ReactionOutput", List.of(
            COMPOUND,
            EQ,
            ANCHOR,
            OUTPUT_NAME,
            CHEMICAL_NAME,
            TYPE,
            THEO_MOL,
            THEO_WEIGHT,
            SAMPLES
    ));
}
