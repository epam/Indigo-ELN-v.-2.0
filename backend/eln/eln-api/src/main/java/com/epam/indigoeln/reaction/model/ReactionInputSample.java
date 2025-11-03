package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
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

    public static final Metamodel<ReactionInputSample> METAMODEL = new Metamodel<ReactionInputSample>("ReactionInput")
            .accept(ReactionSample::addBaseProperties)
            .anchorProperty("anchor", ReactionInputSample::getAnchor, ReactionInputSample::setAnchor)
            .simpleProperty("sampleId", ReactionInputSample::getSampleId, ReactionInputSample::setSampleId)
            .simpleProperty("chemicalName", ReactionInputSample::getChemicalName, ReactionInputSample::setChemicalName)
            .enteredValueProperty("mol", ReactionInputSample::getMol, ReactionInputSample::setMol)
            .enteredValueProperty("weight", ReactionInputSample::getWeight, ReactionInputSample::setWeight)
            .simpleProperty("comment", ReactionInputSample::getComment, ReactionInputSample::setComment)
            ;

    @JsonBackReference
    private ReactionInput row;

    @NotNull
    private Anchor.InputSample anchor;

    @Nullable
    private UUID sampleId;

    @Nullable
    private String chemicalName;

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
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(mol, this::setMol);
        EnteredValue.prepareToRecalculate(weight, this::setWeight);
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }
}
