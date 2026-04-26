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

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Getter
@Setter
@ToString(exclude = "reaction")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionOutput extends ReactionRow {

    @NotNull
    private OutputAnchor anchor;

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
    private List<@Valid ReactionOutputSample> samples = new ArrayList<>();

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type, boolean intended, String outputName, OutputAnchor anchor, CompoundRef compound) {
        ReactionOutput row = new ReactionOutput();
        row.reaction = reaction;
        row.anchor = anchor;
        row.type = type;
        row.outputName = outputName;
        row.intended = intended;
        row.compound = compound;
        reaction.getOutputs().add(row);
        reaction.validateDuplicateOutputs(row);
        return row;
    }

    public void updateCompound(CompoundRef newCompound) {
        validate(!hasSamplesWithRegistrationStarted(), "Cannot update compound when samples already sent for registration");
        this.compound = newCompound;
        reaction.validateDuplicateOutputs(this);
    }

    public boolean hasSamplesWithRegistrationStarted() {
        return samples.stream()
                .anyMatch(s -> s.getRegistrationStatus() != null);
    }

    @Override
    protected List<? extends AbstractExperimentNode<Reaction>> internalGetSiblings(Reaction parent) {
        return parent.getOutputs();
    }
}
