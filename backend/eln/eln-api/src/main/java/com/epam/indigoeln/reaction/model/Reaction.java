package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ReactionInputValueHandler;
import com.epam.indigoeln.reaction.model.patch.handler.ReactionOutputValueHandler;
import com.epam.indigoeln.reaction.util.ToStringUtil;
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

import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(exclude = "model")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class Reaction implements ExperimentModelNode {

    public static final Metamodel<Reaction, ReactionPatch> METAMODEL = new Metamodel<Reaction, ReactionPatch>("Reaction")
            .anchorProperty("anchor", Reaction::getAnchor, Reaction::setAnchor, ReactionPatch::getAnchor, ReactionPatch::setAnchor)
            .simpleProperty("rxnfile", Reaction::getRxnfile, Reaction::setRxnfile, ReactionPatch::getRxnfile, ReactionPatch::setRxnfile)
            .simpleProperty("rxnVersion", Reaction::getRxnVersion, Reaction::setRxnVersion, ReactionPatch::getRxnVersion, ReactionPatch::setRxnVersion)
            .listProperty("inputs", Reaction::getInputs, Reaction::setInputs, ReactionPatch::getInputs, ReactionPatch::setInputs, ReactionInput.METAMODEL, ReactionInputValueHandler.LIST_INSTANCE)
            .listProperty("outputs", Reaction::getOutputs, Reaction::setOutputs, ReactionPatch::getOutputs, ReactionPatch::setOutputs, ReactionOutput.METAMODEL, ReactionOutputValueHandler.LIST_INSTANCE)
            ;

    @JsonBackReference
    private ExperimentModel model;

    @NotNull
    private Anchor.Reaction anchor;

    @NotNull
    private String rxnfile = "";

    @NotNull
    private Integer rxnVersion = 0;

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionInput> inputs = List.of();

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutput> outputs = List.of();

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
                .map(row -> row.getChemicalName().startsWith("P") ? Ints.tryParse(row.getChemicalName().substring(1)) : null)
                .filter(Objects::nonNull)
                .mapToInt(Integer::valueOf)
                .max().orElse(-1);
        return "P" + (maxUsedNumber + 1);
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }
}
