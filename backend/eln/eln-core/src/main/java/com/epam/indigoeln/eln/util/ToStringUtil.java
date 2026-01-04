package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.metamodel.property.ListProperty;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentNode;
import org.jspecify.annotations.Nullable;

public class ToStringUtil {

    public static <C extends ExperimentNode, P> String toStringBuild(Metamodel<C, P> metamodel, C container) {
        Builder builder = new Builder();
        doBuild(metamodel, container, builder);
        return builder.toString();
    }

    private static <C extends ExperimentNode> void doBuild(Metamodel<C, ?> metamodel, @Nullable C container, Builder builder) {
        if (container == null) {
            builder.text("null");
            return;
        }
        builder.open(metamodel.getName());
        ExperimentModelUtil.walkProperties(metamodel, container, new ExperimentModelUtil.PropertyVisitor() {
            @Override
            public void simpleProperty(ExperimentNode node, ModelProperty<ExperimentNode, ?, ?, ?> property) {
                builder.property(property.name(), property.get(node));
            }
            @Override
            public void beforeList(ExperimentNode node, ListProperty<ExperimentNode, ?, ?, ?, ?> property) {
                builder.open(property.name());
            }
            @Override
            public void afterList(ExperimentNode node, ListProperty<ExperimentNode, ?, ?, ?, ?> property) {
                builder.close();
            }
        });
        builder.close();
    }

    public static class Builder {

        private final StringBuilder str = new StringBuilder();
        private int level = 0;
        private String prefix = "";

        public Builder open(String headerLine) {
            str.append(prefix).append(headerLine).append(" {").append('\n');
            level++;
            prefix = "    ".repeat(level);
            return this;
        }

        public Builder close() {
            level--;
            prefix = "    ".repeat(level);
            str.append(prefix).append('}').append('\n');
            return this;
        }

        public Builder property(String name, @Nullable Object value) {
            if (value != null) {
                String valueStr = name.equals("rxnfile") ? "..." : value.toString();
                str.append(prefix).append(name).append(" = ").append(valueStr).append('\n');
            }
            return this;
        }

        public Builder text(String text) {
            str.append(prefix).append(text).append('\n');
            return this;
        }

        @Override
        public String toString() {
            return str.toString();
        }
    }
}
