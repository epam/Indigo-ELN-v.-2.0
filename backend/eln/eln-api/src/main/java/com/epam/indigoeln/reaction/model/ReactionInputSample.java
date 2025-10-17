package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionInputSample extends ReactionSample implements ExperimentModelNode, ToStringTree {

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
        ReactionInputSample sample = new ReactionInputSample();
        sample.row = row;
        sample.anchor = new Anchor.InputSample(row.getReaction().getModel().generateNextAnchor());
        return sample;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(mol, this::setMol);
        EnteredValue.prepareToRecalculate(weight, this::setWeight);
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("ReactionInputSample")
                .property("anchor", anchor)
                .property("chemicalName", chemicalName)
                .property("sampleId", sampleId)
                .property("mol", mol)
                .property("weight", weight)
                .property("density", density)
                .property("molarity", molarity)
                .property("purity", purity)
                .property("comment", comment)
                .close();
    }
}
