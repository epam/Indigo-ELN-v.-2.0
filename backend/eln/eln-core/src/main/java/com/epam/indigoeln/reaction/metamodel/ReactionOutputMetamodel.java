package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.*;

public class ReactionOutputMetamodel {

    // ReactionRow
    public static final ModelProperty<ReactionOutput, CompoundRef> COMPOUND = property("compound", ReactionRow::getCompound, ReactionOutput::updateCompound);
    public static final ModelProperty<ReactionOutput, EnteredValue<NoUnit>> EQ = enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, EnteredValue.DEFAULT_ONE);
    public static final ModelProperty<ReactionInput, @Nullable Integer> RXN_POSITION = property("rxnPosition", ReactionRow::getRxnPosition, ReactionRow::setRxnPosition);
    // ReactionOutput
    public static final ModelProperty<ReactionOutput, OutputAnchor> ANCHOR = property("anchor", ReactionOutput::getAnchor, null);
    public static final ModelProperty<ReactionOutput, String> OUTPUT_NAME = property("outputName", ReactionOutput::getOutputName, ReactionOutput::setOutputName);
    public static final ModelProperty<ReactionOutput, @Nullable String> CHEMICAL_NAME = property("chemicalName", ReactionOutput::getChemicalName, ReactionOutput::setChemicalName);
    public static final ModelProperty<ReactionOutput, ReactionOutputType> TYPE = property("type", ReactionOutput::getType, ReactionOutput::setType);
    public static final ModelProperty<ReactionOutput, Boolean> INTENDED = property("intended", ReactionOutput::isIntended, ReactionOutput::setIntended);
    public static final ModelProperty<ReactionOutput, @Nullable EnteredValue<MolUnit>> THEO_MOL = enteredValueProperty("theoMol", ReactionOutput::getTheoMol, ReactionOutput::setTheoMol);
    public static final ModelProperty<ReactionOutput, @Nullable EnteredValue<WeightUnit>> THEO_WEIGHT = enteredValueProperty("theoWeight", ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight);
    public static final ModelProperty<ReactionOutput, List<ReactionOutputSample>> SAMPLES = listProperty("samples", ReactionOutput::getSamples, ReactionOutput::setSamples, ReactionOutputSampleMetamodel.INSTANCE);

    public static final Metamodel<ReactionOutput> INSTANCE = new Metamodel<>("ReactionOutput", List.of(
            COMPOUND,
            EQ,
            ANCHOR,
            OUTPUT_NAME,
            CHEMICAL_NAME,
            TYPE,
            INTENDED,
            THEO_MOL,
            THEO_WEIGHT,
            SAMPLES
    ));
}
