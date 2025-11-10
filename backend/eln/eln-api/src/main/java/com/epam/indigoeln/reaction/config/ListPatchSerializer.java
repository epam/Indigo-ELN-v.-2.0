package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class ListPatchSerializer extends JsonSerializer<ListPatch<?>> implements ContextualSerializer {

    @Nullable
    private final JavaType valueType;

    public ListPatchSerializer() {
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
            return new ListPatchSerializer(containedType);
        }
        return this;
    }

    @Override
    public void serialize(ListPatch<?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Preconditions.checkState(valueType != null);
        JsonSerializer<Object> valueSerializer = serializers.findValueSerializer(valueType);
        gen.writeStartObject();

        gen.writeFieldName("$");
        gen.writeNumber(container.getSize());
        for (Map.Entry<Integer, ?> entry : container.getItems().entrySet()) {
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
