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

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Getter
@Setter
@ToString(exclude = "reaction")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInput extends ReactionRow {

    @NotNull
    private InputAnchor anchor;

    @NotNull
    private ReactionRole role;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private String chemicalName;

    @NotEmpty
    @JsonManagedReference
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<@Valid ReactionInputSample> samples = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean limiting;

    public static ReactionInput create(Reaction reaction, ReactionRole role, InputAnchor anchor, CompoundRef compound) {
        ReactionInput row = new ReactionInput();
        row.reaction = reaction;
        row.anchor = anchor;
        row.role = role;
        row.compound = compound;
        reaction.getInputs().add(row);
        validateDuplicateInputs(reaction, row);
        return row;
    }

    public void updateCompound(CompoundRef newCompound) {
        for (ReactionInputSample sample : samples) {
            validate(sample.getSampleId() == null, "Cannot update compound with real samples attached");
        }
        this.compound = newCompound;
        validateDuplicateInputs(reaction, this);
    }

    @Override
    protected List<? extends AbstractExperimentNode<Reaction>> internalGetSiblings(Reaction parent) {
        return parent.getInputs();
    }

    private static void validateDuplicateInputs(Reaction reaction, ReactionInput newInput) {
        for (ReactionInput input : reaction.getInputs()) {
            if (input != newInput) {
                validate(!input.getCompound().compoundKeyEquals(newInput.getCompound()), "Reaction contains duplicate input compounds");
            }
        }
    }
}
