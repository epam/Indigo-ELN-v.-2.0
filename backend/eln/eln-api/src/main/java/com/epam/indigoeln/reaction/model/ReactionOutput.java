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
    private OutputAnchor anchor;

    @NotNull
    private String outputName;

    @Nullable
    private String chemicalName;

    @NotNull
    private ReactionOutputType type;

    @Nullable
    private EnteredValue<MolUnit> theoMol;

    @Nullable
    private EnteredValue<WeightUnit> theoWeight;

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionOutputSample> samples = List.of();

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type, OutputAnchor anchor) {
        ReactionOutput row = new ReactionOutput();
        row.reaction = reaction;
        row.anchor = anchor;
        row.type = type;
        return row;
    }
}
