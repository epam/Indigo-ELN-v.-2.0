package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.metamodel.ExperimentModelMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.EnteredValueProperty;
import com.epam.indigoeln.reaction.metamodel.property.ListProperty;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExperimentModelUtil {

    public static void walk(Metamodel<ExperimentModelNode, ?> metamodel, ExperimentModelNode node, Consumer<ExperimentModelNode> visitor) {
        visitor.accept(node);
        for (ModelProperty<ExperimentModelNode, ?, ?, ?> property : metamodel.getProperties()) {
            if (property instanceof ListProperty<?, ?, ?, ?>) {
                ListProperty<ExperimentModelNode, ExperimentModelNode, Object, Object> listProperty = property.cast();
                List<ExperimentModelNode> items = listProperty.getter().apply(node);
                for (ExperimentModelNode item : items) {
                    walk(listProperty.childModel(), item, visitor);
                }
            }
        }
    }

    public static void walkProperties(Metamodel<ExperimentModelNode, ?> metamodel, ExperimentModelNode node, BiConsumer<ExperimentModelNode, ModelProperty<?, ?, ?, ?>> visitor) {
        for (ModelProperty<ExperimentModelNode, ?, ?, ?> property : metamodel.getProperties()) {
            visitor.accept(node, property);
            if (property instanceof ListProperty<?, ?, ?, ?>) {
                ListProperty<ExperimentModelNode, ExperimentModelNode, Object, Object> listProperty = property.cast();
                List<ExperimentModelNode> items = listProperty.getter().apply(node);
                Metamodel<ExperimentModelNode, Object> childModel = listProperty.childModel();
                for (ExperimentModelNode item : items) {
                    walkProperties(childModel, item, visitor);
                }
            }
        }
    }

    public static void prepareToRecalculate(ExperimentModel model) {
        walkProperties(model, (node, property) -> {
            if (property instanceof EnteredValueProperty<?, ?, ?>) {
                EnteredValueProperty<ExperimentModelNode, NoUnit, Object> enteredValueProperty = property.cast();
                EnteredValue.prepareToRecalculate(enteredValueProperty.get(node), v -> enteredValueProperty.set(node, v), enteredValueProperty.defaultValue());
            }
        });
    }

    public static Set<Pair<ReactionRole, CompoundRef>> collectCompoundRefs(ExperimentModel model) {
        Set<Pair<ReactionRole, CompoundRef>> refs = new HashSet<>();
        walk(model, node -> {
            switch (node) {
                case ReactionInput input -> refs.add(Pair.of(input.getRole(), input.getCompound()));
                case ReactionOutput output -> refs.add(Pair.of(ReactionRole.OUTPUT, output.getCompound()));
                default -> {}
            }
        });
        return refs;
    }

    public static void walk(ExperimentModel model, Consumer<ExperimentModelNode> visitor) {
        //noinspection rawtypes,unchecked
        walk((Metamodel) ExperimentModelMetamodel.INSTANCE, model, visitor);
    }

    public static void walkProperties(ExperimentModel model, BiConsumer<ExperimentModelNode, ModelProperty<ExperimentModelNode, ?, ?, ?>> visitor) {
        //noinspection rawtypes,unchecked
        walkProperties((Metamodel) ExperimentModelMetamodel.INSTANCE, model, (BiConsumer) visitor);
    }
}
