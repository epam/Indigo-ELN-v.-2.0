package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public final class ReactionInput extends ReactionRow implements ExperimentModelNode, ToStringTree {

    @NotNull
    private Anchor.Input anchor;

    @NotNull
    private ReactionRole role;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<ReactionInputSample> samples;

    private boolean limiting;

    public static ReactionInput create(Reaction reaction, ReactionRole role) {
        ReactionInput input = new ReactionInput();
        input.reaction = reaction;
        input.anchor = new Anchor.Input(reaction.getModel().generateNextAnchor());
        input.role = role;
        return input;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(mol, this::setMol);
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("ReactionInput")
                .property("anchor", anchor)
                .property("compound", compound)
                .property("eq", eq)
                .property("role", role)
                .property("limiting", limiting)
                .open("samples").nest(samples).close()
                .close();
    }
}
