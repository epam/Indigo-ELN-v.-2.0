package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
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
public final class ReactionOutputSample extends ReactionSample implements ExperimentModelNode, ToStringTree {

    @JsonBackReference
    private ReactionOutput row;

    @Nullable
    private EnteredValue<MolUnit> actualMol;

    @Nullable
    private EnteredValue<WeightUnit> actualWeight;

    @Nullable
    private EnteredValue<NoUnit> yield;

    @Nullable
    private SampleRegistrationStatus registrationStatus;

    @Nullable
    private UUID sampleId;

    @Nullable
    private String strCode;

    public ReactionOutputSample(ReactionOutput row, UUID anchor) {
        this.row = row;
        this.anchor = anchor;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(actualMol, this::setActualMol);
        EnteredValue.prepareToRecalculate(actualWeight, this::setActualWeight);
        EnteredValue.prepareToRecalculate(yield, this::setYield);
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("ReactionOutputSample")
                .property("anchor", anchor)
                .property("actualMol", actualMol)
                .property("actualWeight", actualWeight)
                .property("density", density)
                .property("molarity", molarity)
                .property("volume", volume)
                .property("purity", purity)
                .property("yield", yield)
                .property("registrationStatus", registrationStatus)
                .property("sampleId", sampleId)
                .property("strCode", strCode)
                .close();
    }
}
