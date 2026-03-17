package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.units.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public sealed abstract class ReactionSample<P extends ReactionRow> extends AbstractExperimentNode<P> permits ReactionInputSample, ReactionOutputSample {

    @JsonBackReference
    protected P row;

    @Nullable
    protected EnteredValue<DensityUnit> density;

    @Nullable
    protected EnteredValue<MolarityUnit> molarity;

    @Nullable
    protected EnteredValue<VolumeUnit> volume;

    @NotNull
    protected EnteredValue<NoUnit> purity;

    @Nullable
    protected STRCodeSample strCode;

    @NotNull
    protected List<DictionaryItemRef> healthHazards = new ArrayList<>();

    @JsonIgnore
    public Double getPurityAsFraction() {
        return purity.getValue() * 0.01;
    }

    @Override
    protected P internalGetParent() {
        return row;
    }

    @Override
    protected void internalSetParent(P parent) {
        row = parent;
    }
}
