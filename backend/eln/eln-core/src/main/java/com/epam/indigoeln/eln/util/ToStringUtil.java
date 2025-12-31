package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.metamodel.property.ListProperty;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ToStringUtil {

    public static <C, P> String toStringBuild(Metamodel<C, P> metamodel, C container) {
        Builder builder = new Builder();
        doBuild(metamodel, container, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <C, P> void doBuild(Metamodel<C, P> metamodel, @Nullable C container, Builder builder) {
        if (container == null) {
            builder.text("null");
            return;
        }
        builder.open(metamodel.getName());
        for (ModelProperty<C, ?, P, ?> property : metamodel.getProperties()) {
            if (property instanceof ListProperty) {
                ListProperty<C, Object, P, Object> listProperty = property.cast();
                builder.open(listProperty.name());
                List<Object> children = listProperty.get(container);
                for (Object child : children) {
                    doBuild(listProperty.childModel(), child, builder);
                }
                builder.close();
            } else {
                builder.property(property.name(), property.get(container));
            }
        }
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
