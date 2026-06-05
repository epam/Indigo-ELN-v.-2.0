package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.annotation.*;
import com.google.common.primitives.Ints;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@ToString(exclude = "model")
@EqualsAndHashCode(exclude = "model", callSuper = false)
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Reaction implements ExperimentNode {

    @JsonBackReference
    private final ExperimentModel model;

    @NotNull
    private final ReactionAnchor anchor;

    @Nullable
    @Size(min = 1)
    private String rxnfile;

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionInput> inputs = List.of();

    @Nullable
    private InputAnchor limitingAnchor;

    @NotNull
    @JsonManagedReference
    private List<@Valid ReactionOutput> outputs = List.of();

    public static Reaction create(ExperimentModel model, ReactionAnchor anchor) {
        Reaction reaction = new Reaction(model, anchor);
        model.setReactions(ModelUtil.appendToList(model.getReactions(), reaction));
        return reaction;
    }

    @Nullable
    @JsonIgnore
    public ReactionInput getLimitingInput() {
        for (ReactionInput input : inputs) {
            if (input.getAnchor().equals(limitingAnchor)) {
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

    @Nullable
    public ReactionOutput findOutput(UUID compoundID) {
        for (ReactionOutput output : outputs) {
            if (compoundID.equals(output.getCompound().getCompoundID())) {
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
}
