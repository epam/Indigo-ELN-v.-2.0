package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ListPatchDeserializer extends JsonDeserializer<ListPatch<?>> implements ContextualDeserializer {

    @Nullable
    private final JavaType valueType;

    public ListPatchDeserializer() {
        valueType = null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType type = ctxt.getContextualType();
        if (type != null && type.containedTypeCount() > 0) {
            JavaType containedType = type.containedType(0);
            return new ListPatchDeserializer(containedType);
        }
        return this;
    }

    @Override
    public ListPatch<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Preconditions.checkState(valueType != null);
        if (p.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected START_OBJECT");
        }

        Map<Integer, @Nullable Object> items = new HashMap<>();
        int size = -1;

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.currentName();
            p.nextToken(); // move to value

            // Check if this is the info field (integer)
            if ("$".equals(fieldName)) {
                size = p.getIntValue();
            } else {
                int index = Integer.parseInt(fieldName);
                // Deserialize as type T
                items.put(index, ctxt.readValue(p, valueType));
            }
        }

        return new ListPatch<>(size, items);
    }
}
