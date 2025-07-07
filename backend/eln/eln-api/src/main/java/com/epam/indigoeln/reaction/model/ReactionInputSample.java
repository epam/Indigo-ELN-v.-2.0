package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Nullable
    private UUID sampleId;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private EnteredValue<WeightUnit> weight;

    public ReactionInputSample(ReactionInput row, UUID anchor) {
        this.row = row;
        this.anchor = anchor;
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
                .property("sampleId", sampleId)
                .property("mol", mol)
                .property("weight", weight)
                .property("density", density)
                .property("molarity", molarity)
                .property("purity", purity)
                .close();
    }
}
