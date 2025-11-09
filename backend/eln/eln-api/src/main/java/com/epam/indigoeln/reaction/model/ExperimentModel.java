package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.metamodel.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ReactionValueHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.ExperimentModelUtil;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Data
@EqualsAndHashCode(exclude = "lastUsedAnchorCached")
public final class ExperimentModel implements ExperimentModelNode {

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> METAMODEL = new Metamodel<ExperimentModel, ExperimentModelPatch>("ExperimentModel")
            .simpleProperty("revision", ExperimentModel::getRevision, ExperimentModel::setRevision, ExperimentModelPatch::getRevision, ExperimentModelPatch::setRevision)
            .listProperty("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ExperimentModelPatch::getReactions, ExperimentModelPatch::setReactions, Reaction.METAMODEL, ReactionValueHandler.LIST_INSTANCE)
            ;

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<Reaction> reactions = List.of();

    @Nullable
    @JsonIgnore
    private Integer lastUsedAnchorCached;

    @NotNull
    private Integer revision;

    public int generateNextAnchor() {
        if (lastUsedAnchorCached == null) {
            int[] last = {0};
            walk(node -> {
                Anchor anchor = switch (node) {
                    case Reaction reaction -> reaction.getAnchor();
                    case ReactionInput input -> input.getAnchor();
                    case ReactionInputSample sample -> sample.getAnchor();
                    case ReactionOutput output -> output.getAnchor();
                    case ReactionOutputSample sample -> sample.getAnchor();
                    default -> null;
                };
                if (anchor != null) {
                    last[0] = Math.max(last[0], anchor.getNumber());
                }
            });
            lastUsedAnchorCached = last[0];
        }
        return ++lastUsedAnchorCached;
    }

    public int generateNextNbkBatchNumber() {
        int[] last = {0};
        walk(node -> {
            if (node instanceof ReactionOutputSample sample) {
                last[0] = Math.max(last[0], sample.getNbkBatchNumber().getOrdinal());
            }
        });
        return last[0] + 1;
    }

    public void prepareToRecalculate() {
        walkProperties((node, property) -> {
            if (property instanceof EnteredValueProperty<?, ?, ?>) {
                EnteredValueProperty<ExperimentModelNode, NoUnit, Object> enteredValueProperty = property.cast();
                System.out.println("!!! " + node.getClass().getSimpleName() + " - " + enteredValueProperty.name());
                EnteredValue.prepareToRecalculate(enteredValueProperty.get(node), v -> enteredValueProperty.set(node, v), enteredValueProperty.defaultValue());
            }
        });
    }

    public Set<DictionaryItemRef> collectDictionaryRefs() {
        Set<@Nullable DictionaryItemRef> refs = new HashSet<>();
        walkProperties((node, property) -> {
            switch (property) {
                case DictionaryProperty<?, ?> dictionaryProperty -> {
                    DictionaryProperty<ExperimentModelNode, Object> cast = dictionaryProperty.cast();
                    refs.add(cast.get(node));
                }
                case DictionaryListProperty<?, ?> dictionaryListProperty -> {
                    DictionaryListProperty<ExperimentModelNode, Object> cast = dictionaryListProperty.cast();
                    refs.addAll(cast.get(node));
                }
                case SimpleListProperty<?, ?, ?> simpleListProperty -> {
                    SimpleListProperty<ExperimentModelNode, Object, Object> cast = simpleListProperty.cast();
                    for (Object item : cast.get(node)) {
                        if (item instanceof HasDictionaryRefs hasDictionaryRefs) {
                            hasDictionaryRefs.collectDictionaryRefs().forEach(refs::add);
                        }
                    }
                }
                case SimpleProperty<?, ?, ?> simpleProperty -> {
                    SimpleProperty<ExperimentModelNode, Object, Object> cast = simpleProperty.cast();
                    Object value = cast.get(node);
                    if (value instanceof HasDictionaryRefs hasDictionaryRefs) {
                        hasDictionaryRefs.collectDictionaryRefs().forEach(refs::add);
                    }
                }
                case AnchorProperty<?, ?, ?> anchorProperty -> {}
                case EnteredValueProperty<?, ?, ?> enteredValueProperty -> {}
                case ListProperty<?, ?, ?, ?> listProperty -> {}
            }
        });
        refs.remove(null);
        //noinspection NullableProblems
        return refs;
    }

    public Set<CompoundRef> collectCompoundRefs() {
        Set<CompoundRef> refs = new HashSet<>();
        walk(node -> {
            if (node instanceof ReactionRow row) {
                refs.add(row.getCompound());
            }
        });
        return refs;
    }

    public Reaction locate(ReactionMutation mutation) {
        return locate(mutation.anchor());
    }

    public Reaction locate(Anchor.Reaction anchor) {
        for (Reaction reaction : reactions) {
            if (reaction.getAnchor().equals(anchor)) {
                return reaction;
            }
        }
        throw new IllegalArgumentException("Experiment doesn't contain reaction with id: " + anchor);
    }

    public ReactionInput locate(ReactionInputMutation mutation) {
        return locate(mutation.anchor());
    }

    public ReactionInput locate(Anchor.Input anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionInput row : reaction.getInputs()) {
                if (row.getAnchor().equals(anchor)) {
                    return row;
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain input with id: " + anchor);
    }

    public ReactionInputSample locate(ReactionInputSampleMutation mutation) {
        return locate(mutation.anchor());
    }

    public ReactionInputSample locate(Anchor.InputSample anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionInput row : reaction.getInputs()) {
                for (ReactionInputSample sample : row.getSamples()) {
                    if (sample.getAnchor().equals(anchor)) {
                        return sample;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain input sample with id: " + anchor);
    }

    public ReactionOutput locate(ReactionOutputMutation mutation) {
        return locate(mutation.anchor());
    }

    public ReactionOutput locate(Anchor.Output anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionOutput row : reaction.getOutputs()) {
                if (row.getAnchor().equals(anchor)) {
                    return row;
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain output with id: " + anchor);
    }

    public ReactionOutputSample locate(ReactionOutputSampleMutation mutation) {
        return locate(mutation.anchor());
    }

    public ReactionOutputSample locate(Anchor.OutputSample anchor) {
        for (Reaction reaction : reactions) {
            for (ReactionOutput row : reaction.getOutputs()) {
                for (ReactionOutputSample sample : row.getSamples()) {
                    if (sample.getAnchor().equals(anchor)) {
                        return sample;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Reaction doesn't contain output sample with id: " + anchor);
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }

    private void walk(Consumer<ExperimentModelNode> visitor) {
        //noinspection rawtypes,unchecked
        ExperimentModelUtil.walk((Metamodel) METAMODEL, this, visitor);
    }

    private void walkProperties(BiConsumer<ExperimentModelNode, ModelProperty<ExperimentModelNode, ?, ?, ?>> visitor) {
        //noinspection rawtypes,unchecked
        ExperimentModelUtil.walkProperties((Metamodel) METAMODEL, this, (BiConsumer) visitor);
    }
}
