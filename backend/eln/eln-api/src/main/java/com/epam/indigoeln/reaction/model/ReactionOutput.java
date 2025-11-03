package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutput extends ReactionRow implements ExperimentModelNode, ToStringTree {

    @NotNull
    private Anchor.Output anchor;

    @NotNull
    private String chemicalName;

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
        output.chemicalName = reaction.generateNextProductName();
        return output;
    }

    public static ReactionOutput createWithAnchor(Reaction reaction, Anchor.Output anchor) {
        ReactionOutput output = new ReactionOutput();
        output.reaction = reaction;
        output.anchor = anchor;
        return output;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(theoMol, this::setTheoMol);
        EnteredValue.prepareToRecalculate(theoWeight, this::setTheoWeight);
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("ReactionOutput")
                .property("anchor", anchor)
                .property("chemicalName", chemicalName)
                .property("compound", compound)
                .property("eq", eq)
                .property("type", type)
                .property("theoMol", theoMol)
                .property("theoWeight", theoWeight)
                .open("samples").nest(samples).close()
                .close();
    }
}
