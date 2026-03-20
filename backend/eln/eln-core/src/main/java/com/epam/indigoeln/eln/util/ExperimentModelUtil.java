package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.metamodel.ExperimentModelMetamodel;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.EnteredValueDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
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
        visitor.beforeNode(node);
        for (ModelProperty<N, ?, ?, ?> property : metamodel.getProperties()) {
            Pair<Metamodel<?, ?>, List<ExperimentNode>> children = doGetChildren(node, property);
            if (children != null) {
                visitor.beforeChildren(node, property.cast());
                for (ExperimentNode child : children.b()) {
                    walkProperties((Metamodel<ExperimentNode, ?>) children.a(), child, visitor);
                }
                visitor.afterChildren(node, property.cast());
            } else {
                visitor.simpleProperty(node, (ModelProperty<ExperimentNode, ?, ?, ?>) property);
            }
        }
        visitor.afterNode(node);
    }

    @Nullable
    private static Pair<Metamodel<?, ?>, List<ExperimentNode>> doGetChildren(ExperimentNode node, ModelProperty<?, ?, ?, ?> property) {
        return switch (property.valueHandler()) {
            case ListDiffHandler<?, ?, ?> listHandler -> {
                ModelProperty<ExperimentNode, List<ExperimentNode>, Object, Object> listProperty = property.cast();
                if (listHandler.getItemHandler() instanceof MetamodelDiffHandler<?, ?> childHandler) {
                    List<ExperimentNode> items = listProperty.getter().apply(node);
                    yield Pair.of(childHandler.getMetamodel(), items);
                }
                yield null;
            }
            case MetamodelDiffHandler<?, ?> metamodelDiffHandler -> {
                ModelProperty<ExperimentNode, ExperimentNode, Object, Object> metamodelProperty = property.cast();
                ExperimentNode childNode = metamodelProperty.getter().apply(node);
                yield Pair.of(metamodelDiffHandler.getMetamodel(), List.of(childNode));
            }
            default -> null;
        };
    }

    public static void prepareToRecalculate(ExperimentModel model) {
        walkProperties(ExperimentModelMetamodel.INSTANCE, model, (node, property) -> {
            if (property.valueHandler() instanceof EnteredValueDiffHandler<?>) {
                ModelProperty<ExperimentNode, EnteredValue<MeasurementUnit>, Object, EnteredValuePatch<MeasurementUnit>> enteredValueProperty = property.cast();
                EnteredValue.prepareToRecalculate(enteredValueProperty.get(node), v -> enteredValueProperty.set(node, v), ((EnteredValueDiffHandler<MeasurementUnit>) enteredValueProperty.valueHandler()).getDefaultValueForCalculations());
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

        default void beforeNode(ExperimentNode node) {
        }

        default void afterNode(ExperimentNode node) {
        }

        void simpleProperty(ExperimentNode node, ModelProperty<ExperimentNode, ?, ?, ?> property);

        default void beforeChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?, ?, ?> property) {
        }

        default void afterChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?, ?, ?> property) {
        }
    }
}
