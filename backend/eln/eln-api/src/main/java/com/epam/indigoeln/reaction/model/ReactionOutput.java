package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutput extends ReactionRow implements ExperimentModelNode, ToStringTree {

    @NotNull
    private Anchor.Output anchor;

    @NotNull
    private ReactionOutputType type;

    @Nullable
    private EnteredValue<MolUnit> theoMol;

    @Nullable
    private EnteredValue<WeightUnit> theoWeight;

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutputSample> samples;

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type) {
        ReactionOutput output = new ReactionOutput();
        output.reaction = reaction;
        output.anchor = new Anchor.Output(reaction.getModel().generateNextAnchor());
        output.type = type;
        return output;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(theoMol, this::setTheoMol);
        EnteredValue.prepareToRecalculate(theoWeight, this::setTheoWeight);
        for (ReactionOutputSample sample : samples) {
            sample.prepareToRecalculate();
        }
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("ReactionOutput")
                .property("anchor", anchor)
                .property("compound", compound)
                .property("eq", eq)
                .property("type", type)
                .property("theoMol", theoMol)
                .property("theoWeight", theoWeight)
                .open("samples").nest(samples).close()
                .close();
    }
}
