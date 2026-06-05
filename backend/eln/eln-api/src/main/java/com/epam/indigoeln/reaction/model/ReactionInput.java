package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.appendToList;
import static com.epam.indigoeln.common.util.ModelUtil.removeFromList;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInput extends ReactionRow {

    @NotNull
    private final InputAnchor anchor;

    @NotNull
    private ReactionRole role;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private String chemicalName;

    @NotEmpty
    @JsonManagedReference
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<@Valid ReactionInputSample> samples = List.of();

    public static ReactionInput create(Reaction reaction, ReactionRole role, InputAnchor anchor, CompoundRef compound) {
        ReactionInput row = new ReactionInput(reaction, anchor);
        row.role = role;
        row.compound = compound;
        reaction.setInputs(appendToList(reaction.getInputs(), row));
        validateDuplicateInputs(reaction, row);
        return row;
    }

    @JsonCreator
    ReactionInput(Reaction reaction, InputAnchor anchor) {
        super(reaction);
        this.anchor = anchor;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isLimiting() {
        return anchor.equals(reaction.getLimitingAnchor());
    }

    public void updateCompound(CompoundRef newCompound) {
        for (ReactionInputSample sample : samples) {
            validate(sample.getSampleId() == null, "Cannot update compound with real samples attached");
        }
        this.compound = newCompound;
        validateDuplicateInputs(reaction, this);
    }

    @Override
    public void delete() {
        reaction.setInputs(removeFromList(reaction.getInputs(), this));
    }

    private static void validateDuplicateInputs(Reaction reaction, ReactionInput newInput) {
        for (ReactionInput input : reaction.getInputs()) {
            if (input != newInput) {
                validate(!input.getCompound().compoundKeyEquals(newInput.getCompound()), "Reaction contains duplicate input compounds");
            }
        }
    }
}
