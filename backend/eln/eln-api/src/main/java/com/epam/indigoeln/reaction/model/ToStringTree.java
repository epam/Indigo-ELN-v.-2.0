package com.epam.indigoeln.reaction.model;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface ToStringTree {

    void toStringTree(Builder builder);

    default String toStringTree() {
        Builder builder = new Builder();
        toStringTree(builder);
        return builder.toString();
    }

    class Builder {

        private final StringBuilder str = new StringBuilder();
        private int level = 0;
        private String prefix = "";

        Builder open(String headerLine) {
            str.append(prefix).append(headerLine).append(" {").append('\n');
            level++;
            prefix = "    ".repeat(level);
            return this;
        }

        Builder close() {
            level--;
            prefix = "    ".repeat(level);
            str.append(prefix).append('}').append('\n');
            return this;
        }

        Builder nest(Collection<? extends ToStringTree> elements) {
            for (ToStringTree element : elements) {
                element.toStringTree(this);
            }
            return this;
        }

        Builder property(String name, @Nullable Object value) {
            if (value != null) {
                str.append(prefix).append(name).append(" = ").append(value).append('\n');
            }
            return this;
        }

        Builder text(Object text) {
            str.append(prefix).append(text).append('\n');
            return this;
        }

        @Override
        public String toString() {
            return str.toString();
        }
    }
}
