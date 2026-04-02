package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.metamodel.ExperimentModelMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentNode;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ExperimentModelUtil {

    public static <N extends ExperimentNode> void walk(Metamodel<N> metamodel, N node, Consumer<ExperimentNode> visitor) {
        visitor.accept(node);
        for (ModelProperty<N, ?> property : metamodel.getProperties()) {
            Pair<Metamodel<?>, Collection<ExperimentNode>> children = doGetChildren(node, property);
            if (children != null) {
                for (ExperimentNode child : children.b()) {
                    //noinspection rawtypes,unchecked
                    walk((Metamodel) children.a(), child, visitor);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <N extends ExperimentNode> void walkProperties(Metamodel<N> metamodel, N node, PropertyVisitor visitor) {
        visitor.beforeNode(node);
        for (ModelProperty<N, ?> property : metamodel.getProperties()) {
            Pair<Metamodel<?>, Collection<ExperimentNode>> children = doGetChildren(node, property);
            if (children != null) {
                visitor.beforeChildren(node, property.cast());
                for (ExperimentNode child : children.b()) {
                    walkProperties((Metamodel<ExperimentNode>) children.a(), child, visitor);
                }
                visitor.afterChildren(node, property.cast());
            } else {
                visitor.simpleProperty(node, (ModelProperty<ExperimentNode, ?>) property);
            }
        }
        visitor.afterNode(node);
    }

    @Nullable
    private static Pair<Metamodel<?>, Collection<ExperimentNode>> doGetChildren(ExperimentNode node, ModelProperty<?, ?> property) {
        if (property.childModel() != null) {
            Object value = property.cast().get(node);
            //noinspection unchecked,rawtypes
            return (Pair) Pair.of(property.childModel(), value instanceof Collection ? (Collection) value : List.of(value));
        }
        return null;
    }

    public static void prepareToRecalculate(ExperimentModel model) {
        walkProperties(ExperimentModelMetamodel.INSTANCE, model, (node, property) -> {
            if (property.isEnteredValue() && property.setter() != null) {
                //noinspection rawtypes,unchecked
                doPrepareToRecalculate(node, (ModelProperty) property);
            }
        });
    }

    public static <U extends MeasurementUnit> void doPrepareToRecalculate(ExperimentNode node, ModelProperty<ExperimentNode, EnteredValue<U>> property) {
        EnteredValue<?> value = property.get(node);
        if ((value != null && value.getSource().isCalculated() || (value == null && property.defaultValue() != null))) {
            property.set(node, property.defaultValue());
        }
        if (value != null) {
            value.setOverwritten(false);
        }
    }

    public interface PropertyVisitor {

        default void beforeNode(ExperimentNode node) {
        }

        default void afterNode(ExperimentNode node) {
        }

        void simpleProperty(ExperimentNode node, ModelProperty<ExperimentNode, ?> property);

        default void beforeChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?> property) {
        }

        default void afterChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?> property) {
        }
    }
}
