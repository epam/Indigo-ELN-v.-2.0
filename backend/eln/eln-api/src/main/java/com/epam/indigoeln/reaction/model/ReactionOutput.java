package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
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
public final class ReactionOutput extends ReactionRow {

    @NotNull
    private final OutputAnchor anchor;

    @NotNull
    private String outputName;

    @Nullable
    private String chemicalName;

    @NotNull
    private ReactionOutputType type;

    @NotNull
    private boolean intended;

    @Nullable
    private EnteredValue<MolUnit> theoMol;

    @Nullable
    private EnteredValue<WeightUnit> theoWeight;

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionOutputSample> samples = List.of();

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type, boolean intended, String outputName, OutputAnchor anchor, CompoundRef compound, EnteredValue<NoUnit> eq) {
        ReactionOutput row = new ReactionOutput(reaction, anchor);
        row.type = type;
        row.outputName = outputName;
        row.intended = intended;
        row.compound = compound;
        row.eq = eq;
        reaction.setOutputs(appendToList(reaction.getOutputs(), row));
        validateDuplicateOutputs(reaction, row);
        return row;
    }

    @JsonCreator
    ReactionOutput(Reaction reaction, OutputAnchor anchor) {
        super(reaction);
        this.anchor = anchor;
    }

    public void updateCompound(CompoundRef newCompound) {
        validate(!hasSamplesWithRegistrationStarted(), "Cannot update compound when samples already sent for registration");
        this.compound = newCompound;
        validateDuplicateOutputs(reaction, this);
    }

    public boolean hasSamplesWithRegistrationStarted() {
        return samples.stream()
                .anyMatch(s -> s.getRegistrationStatus() != null);
    }

    @Override
    public void delete() {
        reaction.setOutputs(removeFromList(reaction.getOutputs(), this));
    }

    private static void validateDuplicateOutputs(Reaction reaction, ReactionOutput newOutput) {
        for (ReactionOutput output : reaction.getOutputs()) {
            if (output != newOutput) {
                validate(!output.getCompound().compoundKeyEquals(newOutput.getCompound()), "Reaction contains duplicate output compounds");
            }
        }
    }
}
