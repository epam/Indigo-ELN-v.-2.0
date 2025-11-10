package com.epam.indigoeln.reaction.model.patch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;

@Value
@AllArgsConstructor
@JsonSerialize(using = ListPatch.Serializer.class)
@JsonDeserialize(using = ListPatch.Deserializer.class)
public class ListPatch<T> {

    int size;
    Map<Integer, @Nullable T> items;

    public ListPatch(int size) {
        this.size = size;
        items = new TreeMap<>();
    }

    public void forEach(BiConsumer<Integer, @Nullable T> action) {
        for (Map.Entry<Integer, @Nullable T> entry : items.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    @AllArgsConstructor
    static class Serializer extends JsonSerializer<ListPatch<?>> implements ContextualSerializer {

        @Nullable
        private final JavaType valueType;

        public Serializer() {
            valueType = null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            JavaType type = property.getType();
            if (type != null && type.getRawClass().equals(Optional.class)) {
                type = type.containedType(0);
            }
            if (type != null && type.containedTypeCount() > 0) {
                JavaType containedType = type.containedType(0);
                return new Serializer(containedType);
            }
            return this;
        }

        @Override
        public void serialize(ListPatch<?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Preconditions.checkState(valueType != null);
            JsonSerializer<Object> valueSerializer = serializers.findValueSerializer(valueType);
            gen.writeStartObject();

            gen.writeFieldName("$");
            gen.writeNumber(container.size);
            for (Map.Entry<Integer, ?> entry : container.items.entrySet()) {
                gen.writeFieldName(Integer.toString(entry.getKey()));
                if (entry.getValue() != null) {
                    valueSerializer.serialize(entry.getValue(), gen, serializers);
                } else {
                    gen.writeNull();
                }
            }

            gen.writeEndObject();
        }
    }

    @AllArgsConstructor
    static class Deserializer extends JsonDeserializer<ListPatch<?>> implements ContextualDeserializer {

        @Nullable
        private final JavaType valueType;

        public Deserializer() {
            valueType = null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType type = ctxt.getContextualType();
            if (type != null && type.containedTypeCount() > 0) {
                JavaType containedType = type.containedType(0);
                return new Deserializer(containedType);
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
                    Object value;
                    value = ctxt.readValue(p, valueType);
                    items.put(index, value);
                }
            }

            return new ListPatch<>(size, items);
        }
    }
}
