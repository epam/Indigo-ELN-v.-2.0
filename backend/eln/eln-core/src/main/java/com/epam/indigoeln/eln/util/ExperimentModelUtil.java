package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.metamodel.ExperimentModelMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.EnteredValueProperty;
import com.epam.indigoeln.reaction.metamodel.property.ListProperty;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ExperimentModelUtil {

    public static <N extends ExperimentNode> void walk(Metamodel<N, ?> metamodel, N node, Consumer<ExperimentNode> visitor) {
        visitor.accept(node);
        for (ModelProperty<N, ?, ?, ?> property : metamodel.getProperties()) {
            Pair<Metamodel<?, ?>, List<ExperimentNode>> children = doGetChildren(node, property);
            if (children != null) {
                for (ExperimentNode child : children.b()) {
                    //noinspection rawtypes,unchecked
                    walk((Metamodel) children.a(), child, visitor);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <N extends ExperimentNode> void walkProperties(Metamodel<N, ?> metamodel, N node, PropertyVisitor visitor) {
        for (ModelProperty<N, ?, ?, ?> property : metamodel.getProperties()) {
            Pair<Metamodel<?, ?>, List<ExperimentNode>> children = doGetChildren(node, property);
            if (children != null) {
                visitor.beforeList(node, (ListProperty<ExperimentNode, ?, ?, ?, ?>) property);
                for (ExperimentNode child : children.b()) {
                    walkProperties((Metamodel<ExperimentNode, ?>) children.a(), child, visitor);
                }
                visitor.afterList(node, (ListProperty<ExperimentNode, ?, ?, ?, ?>) property);
            } else {
                visitor.simpleProperty(node, (ModelProperty<ExperimentNode, ?, ?, ?>) property);
            }
        }
    }

    @Nullable
    private static Pair<Metamodel<?, ?>, List<ExperimentNode>> doGetChildren(ExperimentNode node, ModelProperty<?, ?, ?, ?> property) {
        if (property instanceof ListProperty<?, ?, ?, ?, ?>) {
            ListProperty<ExperimentNode, ExperimentNode, Object, Object, Object> listProperty = property.cast();
            if (listProperty.valueHandler().getItemHandler() instanceof MetamodelDiffHandler<?, ?> childHandler) {
                List<ExperimentNode> items = listProperty.getter().apply(node);
                return Pair.of(childHandler.getMetamodel(), items);
            }
        }
        return null;
    }

    public static void prepareToRecalculate(ExperimentModel model) {
        walkProperties(ExperimentModelMetamodel.INSTANCE, model, (node, property) -> {
            if (property instanceof EnteredValueProperty<?, ?, ?>) {
                EnteredValueProperty<ExperimentNode, MeasurementUnit, Object> enteredValueProperty = property.cast();
                EnteredValue.prepareToRecalculate(enteredValueProperty.get(node), v -> enteredValueProperty.set(node, v), enteredValueProperty.defaultValue());
            }
        });
    }

    public static Set<Pair<ReactionRole, CompoundRef>> collectCompoundRefs(ExperimentModel model) {
        Set<Pair<ReactionRole, CompoundRef>> refs = new HashSet<>();
        walk(ExperimentModelMetamodel.INSTANCE, model, node -> {
            switch (node) {
                case ReactionInput input -> refs.add(Pair.of(input.getRole(), input.getCompound()));
                case ReactionOutput output -> refs.add(Pair.of(ReactionRole.OUTPUT, output.getCompound()));
                default -> {}
            }
        });
        return refs;
    }

    public interface PropertyVisitor {

        void simpleProperty(ExperimentNode node, ModelProperty<ExperimentNode, ?, ?, ?> property);

        default void beforeList(ExperimentNode node, ListProperty<ExperimentNode, ?, ?, ?, ?> property) {
        }

        default void afterList(ExperimentNode node, ListProperty<ExperimentNode, ?, ?, ?, ?> property) {
        }
    }
}
