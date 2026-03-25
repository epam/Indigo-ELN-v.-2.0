package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentNode;
import org.jspecify.annotations.Nullable;

public class ToStringUtil {

    public static <C extends ExperimentNode, P> String toStringBuild(Metamodel<C> metamodel, C container) {
        Builder builder = new Builder();
        doBuild(metamodel, container, builder);
        return builder.toString();
    }

    private static <C extends ExperimentNode> void doBuild(Metamodel<C> metamodel, @Nullable C container, Builder builder) {
        if (container == null) {
            builder.text("null");
            return;
        }
        ExperimentModelUtil.walkProperties(metamodel, container, new ExperimentModelUtil.PropertyVisitor() {
            @Override
            public void beforeNode(ExperimentNode node) {
                builder.open(node.getClass().getSimpleName());
            }
            @Override
            public void afterNode(ExperimentNode node) {
                builder.close();
            }
            @Override
            public void simpleProperty(ExperimentNode node, ModelProperty<ExperimentNode, ?> property) {
                builder.property(property.name(), property.get(node));
            }
            @Override
            public void beforeChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?> property) {
                builder.open(property.name());
            }
            @Override
            public void afterChildren(ExperimentNode node, ModelProperty<ExperimentNode, ?> property) {
                builder.close();
            }
        });
    }

    public static class Builder {

        private final StringBuilder str = new StringBuilder();
        private int level = 0;
        private String prefix = "";
        private String prefix2 = "";
        private String suffix2 = "";

        public Builder setPrefixAndSuffix2(String prefix2, String suffix2) {
            this.prefix2 = prefix2;
            this.suffix2 = suffix2;
            return this;
        }

        public Builder open(String headerLine) {
            str.append(prefix).append(headerLine).append(" {").append('\n');
            level++;
            prefix = "    ".repeat(level);
            return this;
        }

        public Builder open2(String headerLine) {
            str.append(prefix).append(prefix2).append(headerLine).append(suffix2).append('\n');
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

        public Builder close2() {
            level--;
            prefix = "    ".repeat(level);
            return this;
        }

        public Builder property(String name, @Nullable Object value) {
            if (value != null) {
                String valueStr = name.equals("rxnfile") ? "..." : value.toString();
                str.append(prefix).append(prefix2).append(name).append(" = ").append(valueStr).append(suffix2).append('\n');
            }
            return this;
        }

        public Builder text(String text) {
            str.append(prefix).append(prefix2).append(text).append(suffix2).append('\n');
            return this;
        }

        @Override
        public String toString() {
            return str.toString();
        }
    }
}
