package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.annotation.*;
import com.google.common.primitives.Ints;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@ToString(exclude = "model")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Reaction extends AbstractExperimentNode<ExperimentModel> {

    @JsonBackReference
    private ExperimentModel model;

    @NotNull
    private ReactionAnchor anchor;

    @Nullable
    @Size(min = 1)
    private String rxnfile;

    @NotNull
    private Integer rxnVersion; // = 0

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionInput> inputs = new ArrayList<>();

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionOutput> outputs = new ArrayList<>();

    public static Reaction create(ExperimentModel model, ReactionAnchor anchor) {
        Reaction reaction = new Reaction();
        reaction.model = model;
        reaction.anchor = anchor;
        reaction.rxnVersion = 0;
        return reaction;
    }

    @JsonIgnore
    @AssertTrue(message = "Reaction must have one and only one limiting input")
    public boolean isOnlyOneLimitingInput() {
        if (inputs.isEmpty()) {
            return true;
        }
        int count = 0;
        for (ReactionInput input : inputs) {
            if (input.isLimiting()) {
                count++;
            }
        }
        return count == 1;
    }

    @Nullable
    @JsonIgnore
    public ReactionInput getLimitingInput() {
        for (ReactionInput input : inputs) {
            if (input.isLimiting()) {
                return input;
            }
        }
        return null;
    }

    @Nullable
    @JsonIgnore
    public ReactionOutput getFinalOutput() {
        for (ReactionOutput output : outputs) {
            if (output.getType() == ReactionOutputType.FINAL) {
                return output;
            }
        }
        return null;
    }

    public String generateNextProductName() {
        int maxUsedNumber = outputs.stream()
                .map(row -> row.getOutputName().startsWith("P") ? Ints.tryParse(row.getOutputName().substring(1)) : null)
                .filter(Objects::nonNull)
                .mapToInt(Integer::valueOf)
                .max().orElse(-1);
        return "P" + (maxUsedNumber + 1);
    }

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<STRCodeSample> getPrecursorReactantIds() {
        return StreamEx.of(inputs)
                .filter(r -> r.getRole() == ReactionRole.REACTANT)
                .flatMap(r -> r.getSamples().stream())
                .map(ReactionSample::getStrCode)
                .collect(StreamUtil.toListNotNull());
    }

    @Override
    protected ExperimentModel internalGetParent() {
        return model;
    }

    @Override
    protected void internalSetParent(ExperimentModel parent) {
        model = parent;
    }

    @Override
    protected List<? extends AbstractExperimentNode<ExperimentModel>> internalGetSiblings(ExperimentModel parent) {
        return parent.getReactions();
    }
}
