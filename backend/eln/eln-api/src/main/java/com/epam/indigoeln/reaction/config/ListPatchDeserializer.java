package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class ListPatchDeserializer extends JsonDeserializer<ListPatch<?, ?>> implements ContextualDeserializer {

    private final Class<?> keyType;
    private final JavaType valueType;

    @SuppressWarnings("DataFlowIssue")
    public ListPatchDeserializer() {
        keyType = null;
        valueType = null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType type = ctxt.getContextualType();
        if (type != null) {
            Class<?> keyType = ((SimpleType) type.containedType(0)).getRawClass();
            JavaType valueType = type.containedType(1);
            return new ListPatchDeserializer(keyType, valueType);
        }
        return this;
    }

    @Override
    public ListPatch<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Preconditions.checkState(valueType != null);
        if (p.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected START_OBJECT");
        }

        Map<Object, @Nullable Object> items = new HashMap<>();
        int size = -1;

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.currentName();
            p.nextToken();

            if (ListPatch.SIZE_FIELD.equals(fieldName)) {
                size = p.getIntValue();
            } else {
                Comparable<?> index;
                if (keyType == Integer.class) {
                    index = Integer.parseInt(fieldName);
                } else if (keyType == UUID.class) {
                    index = UUID.fromString(fieldName);
                } else {
                    throw new IllegalArgumentException(keyType.getName());
                }
                items.put(index, ctxt.readValue(p, valueType));
            }
        }

        //noinspection rawtypes,unchecked
        return new ListPatch(size, items);
    }
}
