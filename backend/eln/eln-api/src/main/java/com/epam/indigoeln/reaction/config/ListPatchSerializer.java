package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class ListPatchSerializer extends JsonSerializer<ListPatch<?, ?>> implements ContextualSerializer {

    private final JsonSerializer<Object> valueSerializer;

    @SuppressWarnings("DataFlowIssue")
    public ListPatchSerializer() {
        valueSerializer = null;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        JavaType type = property.getType();
        if (type != null && type.getRawClass().equals(Optional.class)) {
            type = type.containedType(0);
        }
        if (type != null) {
            JavaType valueType = type.containedType(1);
            return new ListPatchSerializer(prov.findValueSerializer(valueType));
        }
        return this;
    }

    @Override
    public void serialize(ListPatch<?, ?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeFieldName(ListPatch.SIZE_FIELD);
        gen.writeNumber(container.getSize());
        for (Map.Entry<?, ?> entry : container.getItems().entrySet()) {
            gen.writeFieldName(entry.getKey().toString());
            if (entry.getValue() != null) {
                valueSerializer.serialize(entry.getValue(), gen, serializers);
            } else {
                gen.writeNull();
            }
        }

        gen.writeEndObject();
    }
}
