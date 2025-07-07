package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Reaction implements ExperimentModelNode, ToStringTree {

    @JsonBackReference
    private ExperimentModel model;

    @NotNull
    private UUID anchor;

    @NotNull
    private String molFile = "";

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionInput> inputs = new ArrayList<>(0);

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutput> outputs = new ArrayList<>(0);

    public Reaction(ExperimentModel model, UUID anchor) {
        this.model = model;
        this.anchor = anchor;
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

    @JsonIgnore
    public int getReactionNo() {
        return model.getReactions().indexOf(this);
    }

    @Override
    public void prepareToRecalculate() {
        for (ReactionInput input : inputs) {
            input.prepareToRecalculate();
        }
        for (ReactionOutput output : outputs) {
            output.prepareToRecalculate();
        }
    }

    @Override
    public String toString() {
        return toStringTree();
    }
}
