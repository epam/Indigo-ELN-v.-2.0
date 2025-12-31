package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import lombok.AllArgsConstructor;

import java.io.IOException;

public class PatchedSerializers {

    public static final String FIELD_OLD = "$old";
    public static final String FIELD_NEW = "$new";

    private static SerializerUtils.ContentType detectTypeParameter(BeanProperty property) {
        JavaType type = SerializerUtils.findContextType(property);
        // type is Patched<T> or Map<?, Patched<T>>
        // for maps, map (de)serializer seems to pass property as is, so property points to map, instead of map key or value
        if (type.isMapLikeType()) { // for maps, assume Patched is value
            type = type.containedType(1);
        }
        if (!Patched.class.equals(type.getRawClass())) {
            throw new IllegalStateException("Cannot determine value type for PatchedSerializers");
        }
        JavaType contentType = type.containedType(0); // T
        BeanProperty contentProperty = SerializerUtils.createSyntheticProperty(contentType);
        return new SerializerUtils.ContentType(contentType, contentProperty);
    }

    @AllArgsConstructor
    public static class Serializer extends JsonSerializer<Patched<?>> implements ContextualSerializer {

        private final JsonSerializer<Object> valueSerializer;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Serializer() {
            valueSerializer = null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ContentType contentTypes = detectTypeParameter(property);

            return new Serializer(SerializerUtils.findValueSerializer(prov, contentTypes));
        }

        @Override
        public void serialize(Patched<?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (container.value() != null && container.oldValue() == null) { // created or verbatim
                System.out.println(this + " serialize: writing: " + container.value());
                valueSerializer.serialize(container.value(), gen, serializers);
            } else {
                gen.writeStartObject();
                if (container.oldValue() != null) {
                    gen.writeFieldName(FIELD_OLD);
                    valueSerializer.serialize(container.oldValue(), gen, serializers);
                }
                if (container.value() != null) {
                    gen.writeFieldName(FIELD_NEW);
                    valueSerializer.serialize(container.value(), gen, serializers);
                }
                gen.writeEndObject();
            }
        }
    }

    @AllArgsConstructor
    public static class Deserializer extends JsonDeserializer<Patched<?>> implements ContextualDeserializer {

        private final JsonDeserializer<Object> valueDeserializer;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Deserializer() {
            valueDeserializer = null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ContentType contentType = detectTypeParameter(property);
            return new Deserializer(SerializerUtils.findValueDeserializer(ctxt, contentType));
        }

        @Override
        public Patched<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() == JsonToken.START_OBJECT) {
                // Here, we have two options: either object with $old/$new, or verbatim copy of value object.
                // To detect it, we need to read the first field name; however, if it's not $old/$new, value deserializer must start reading from
                // two tokens back (START_OBJECT and FIELD_NAME). To make it work, use TokenBuffer to allow two iterations over the same tokens.
                TokenBuffer buffer = new TokenBuffer(p, ctxt);
                buffer.copyCurrentStructure(p);

                JsonParser p2 = buffer.asParser();
                p2.nextToken(); // step to start object
                String fieldName = p2.nextFieldName(); // step to field name

                // try to read $old/$new
                if (FIELD_OLD.equals(fieldName) || FIELD_NEW.equals(fieldName)) {
                    Object oldValue = null, newValue = null;
                    do {
                        p2.nextToken(); // step to field value
                        Object fieldValue = valueDeserializer.deserialize(p2, ctxt);
                        switch (fieldName) {
                            case FIELD_OLD -> oldValue = fieldValue;
                            case FIELD_NEW -> newValue = fieldValue;
                            default -> throw new IllegalStateException("Unexpected field: " + p2.currentName() + ", expecting " + FIELD_OLD + " or " + FIELD_NEW);
                        }
                        fieldName = p2.nextFieldName();
                    } while (fieldName != null);
                    return new Patched<>(oldValue, newValue);
                }

                // read as regular value object instead
                JsonParser p3 = buffer.asParser();
                p3.nextToken(); // step to START_OBJECT
                Object value = valueDeserializer.deserialize(p3, ctxt);
                return Patched.created(value);
            }

            // if it's not START_OBJECT, read value directly
            Object value = valueDeserializer.deserialize(p, ctxt);
            return Patched.created(value);
        }
    }
}
