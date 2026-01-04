package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@ToString(exclude = "reaction")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInput extends ReactionRow implements ExperimentNode {

    @NotNull
    private Anchor.Input anchor;

    @NotNull
    private ReactionRole role;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private String chemicalName;

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<ReactionInputSample> samples = List.of();

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean limiting;

    public static ReactionInput create(Reaction reaction, ReactionRole role) {
        ReactionInput input = createWithAnchor(reaction, new Anchor.Input(reaction.getModel().generateNextAnchor()));
        input.role = role;
        return input;
    }

    public static ReactionInput createWithAnchor(Reaction reaction, Anchor.Input anchor) {
        ReactionInput input = new ReactionInput();
        input.reaction = reaction;
        input.anchor = anchor;
        return input;
    }
}
