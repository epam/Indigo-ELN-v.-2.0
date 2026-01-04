package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
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
public final class ReactionOutput extends ReactionRow implements ExperimentNode {

    @NotNull
    private Anchor.Output anchor;

    @NotNull
    private String outputName;

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
        output.outputName = reaction.generateNextProductName();
        return output;
    }

    public static ReactionOutput createWithAnchor(Reaction reaction, Anchor.Output anchor) {
        ReactionOutput output = new ReactionOutput();
        output.reaction = reaction;
        output.anchor = anchor;
        return output;
    }
}
