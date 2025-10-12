package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(exclude = "model")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class Reaction implements ExperimentModelNode, ToStringTree {

    @JsonBackReference
    private ExperimentModel model;

    @NotNull
    private Anchor.Reaction anchor;

    @NotNull
    private String rxnfile = "";

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionInput> inputs = new ArrayList<>(0);

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutput> outputs = new ArrayList<>(0);

    public static Reaction create(ExperimentModel model) {
        Reaction reaction = new Reaction();
        reaction.model = model;
        reaction.anchor = new Anchor.Reaction(model.generateNextAnchor());
        return reaction;
    }

    @Override
    public void toStringTree(Builder builder) {
        builder.open("Reaction")
                .property("anchor", anchor)
                .open("inputs").nest(inputs).close()
                .open("outputs").nest(outputs).close()
                .close();
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

    public Iterable<ReactionInput> getInputsOfType(ReactionRole role) {
        return Iterables.filter(inputs, input -> input.getRole() == role);
    }

    public String generateNextOutputName() {
        int maxUsedNumber = outputs.stream()
                .map(row -> row.getName().startsWith("P") ? Ints.tryParse(row.getName().substring(1)) : null)
                .filter(Objects::nonNull)
                .mapToInt(Integer::valueOf)
                .max().orElse(-1);
        return "P" + (maxUsedNumber + 1);
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
