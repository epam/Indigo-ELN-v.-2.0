package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutput extends ReactionRow implements ExperimentModelNode {

    public static void buildMetamodel(Metamodel<ReactionOutput, ReactionOutputPatch> metamodel) {
        metamodel.setName("ReactionOutput");
        metamodel.property("anchor", ReactionOutput::getAnchor, ReactionOutput::setAnchor, ReactionOutputPatch::getAnchor, ReactionOutputPatch::setAnchor);
        metamodel.accept(ReactionRow::buildMetamodelBase);
        metamodel.property("outputName", ReactionOutput::getOutputName, ReactionOutput::setOutputName, ReactionOutputPatch::getOutputName, ReactionOutputPatch::setOutputName);
        metamodel.property("type", ReactionOutput::getType, ReactionOutput::setType, ReactionOutputPatch::getType, ReactionOutputPatch::setType);
        metamodel.enteredValueProperty("theoMol", ReactionOutput::getTheoMol, ReactionOutput::setTheoMol, ReactionOutputPatch::getTheoMol, ReactionOutputPatch::setTheoMol);
        metamodel.enteredValueProperty("theoWeight", ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight, ReactionOutputPatch::getTheoWeight, ReactionOutputPatch::setTheoWeight);
        metamodel.modelListProperty("samples", ReactionOutput::getSamples, ReactionOutput::setSamples, ReactionOutputPatch::getSamples, ReactionOutputPatch::setSamples, Handlers.OUTPUT_SAMPLE_METAMODEL, Handlers.REACTION_OUTPUT_SAMPLE_LIST);
    }

    @NotNull
    private Anchor.Output anchor;

    @NotNull
    private String outputName;

    @NotNull
    private ReactionOutputType type;

    @Nullable
    private EnteredValue<MolUnit> theoMol;

    @Nullable
    private EnteredValue<WeightUnit> theoWeight;

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutputSample> samples = List.of();

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type) {
        ReactionOutput output = createWithAnchor(reaction, new Anchor.Output(reaction.getModel().generateNextAnchor()));
        output.type = type;
        output.outputName = reaction.generateNextProductName();
        return output;
    }

    public static ReactionOutput createWithAnchor(Reaction reaction, Anchor.Output anchor) {
        ReactionOutput output = new ReactionOutput();
        output.reaction = reaction;
        output.anchor = anchor;
        return output;
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(Handlers.OUTPUT_METAMODEL, this);
    }
}
