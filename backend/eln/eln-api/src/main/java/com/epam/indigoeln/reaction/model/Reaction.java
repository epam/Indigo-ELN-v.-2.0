package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Data
@ToString(exclude = "model")
@EqualsAndHashCode(exclude = "model")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Reaction implements ExperimentNode {

    @JsonBackReference
    private ExperimentModel model;

    @NotNull
    private Anchor.Reaction anchor;

    @Nullable
    @Length(min = 1)
    private String rxnfile;

    @NotNull
    private Integer rxnVersion = 0;

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionInput> inputs = List.of();

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionOutput> outputs = List.of();

    public static Reaction create(ExperimentModel model) {
        return createWithAnchor(model, new Anchor.Reaction(model.generateNextAnchor()));
    }

    public static Reaction createWithAnchor(ExperimentModel model, Anchor.Reaction anchor) {
        Reaction reaction = new Reaction();
        reaction.model = model;
        reaction.anchor = anchor;
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

    public Iterable<ReactionInput> inputsOfType(ReactionRole role) {
        return Iterables.filter(inputs, input -> input.getRole() == role);
    }

    public String generateNextProductName() {
        int maxUsedNumber = outputs.stream()
                .map(row -> row.getOutputName().startsWith("P") ? Ints.tryParse(row.getOutputName().substring(1)) : null)
                .filter(Objects::nonNull)
                .mapToInt(Integer::valueOf)
                .max().orElse(-1);
        return "P" + (maxUsedNumber + 1);
    }
}
