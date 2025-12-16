package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.ExperimentModelNode;
import com.epam.indigoeln.reaction.model.metamodel.ListProperty;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.metamodel.ModelProperty;

import java.util.List;
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
}
