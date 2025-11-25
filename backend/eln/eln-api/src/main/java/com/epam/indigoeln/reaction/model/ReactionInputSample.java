package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionInputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = "row")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionInputSample extends ReactionSample implements ExperimentModelNode {

    public static void buildMetamodel(Metamodel<ReactionInputSample, ReactionInputSamplePatch> metamodel) {
        metamodel.setName("ReactionInputSample");
        metamodel.anchorProperty("anchor", ReactionInputSample::getAnchor, ReactionInputSample::setAnchor, ReactionInputSamplePatch::getAnchor, ReactionInputSamplePatch::setAnchor);
        metamodel.accept(ReactionSample::buildMetamodelBase);
        metamodel.<@Nullable UUID>simpleProperty("sampleId", ReactionInputSample::getSampleId, ReactionInputSample::setSampleId, ReactionInputSamplePatch::getSampleId, ReactionInputSamplePatch::setSampleId);
        metamodel.<@Nullable String>simpleProperty("chemicalName", ReactionInputSample::getChemicalName, ReactionInputSample::setChemicalName, ReactionInputSamplePatch::getChemicalName, ReactionInputSamplePatch::setChemicalName);
        metamodel.enteredValueProperty("mol", ReactionInputSample::getMol, ReactionInputSample::setMol, ReactionInputSamplePatch::getMol, ReactionInputSamplePatch::setMol);
        metamodel.enteredValueProperty("weight", ReactionInputSample::getWeight, ReactionInputSample::setWeight, ReactionInputSamplePatch::getWeight, ReactionInputSamplePatch::setWeight);
        metamodel.<@Nullable String>simpleProperty("comment", ReactionInputSample::getComment, ReactionInputSample::setComment, ReactionInputSamplePatch::getComment, ReactionInputSamplePatch::setComment);
    }

    @JsonBackReference
    private ReactionInput row;

    @NotNull
    private Anchor.InputSample anchor;

    @Nullable
    private UUID sampleId;

    @Nullable
    private String chemicalName;

    @Nullable
    private NbkBatchNumber nbkBatchNumber;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private EnteredValue<WeightUnit> weight;

    @Nullable
    private String comment;

    public static ReactionInputSample create(ReactionInput row) {
        return createWithAnchor(row, new Anchor.InputSample(row.getReaction().getModel().generateNextAnchor()));
    }

    public static ReactionInputSample createWithAnchor(ReactionInput row, Anchor.InputSample anchor) {
        ReactionInputSample sample = new ReactionInputSample();
        sample.row = row;
        sample.anchor = anchor;
        return sample;
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(Handlers.INPUT_SAMPLE_METAMODEL, this);
    }
}
