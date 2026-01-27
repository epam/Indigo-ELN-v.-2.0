package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class PatchedSerializers {

    public static final String FIELD_OLD = "$old";
    public static final String FIELD_NEW = "$new";

    private static SerializerUtils.ValueAndPatchContentType detectTypeParameter(BeanProperty property) {
        JavaType type = SerializerUtils.findContextType(property);
        // type is Patched<T> or Map<?, Patched<T>>
        // for maps, map (de)serializer seems to pass property as is, so property points to map, instead of map key or value
        if (type.isMapLikeType()) { // for maps, assume Patched is value
            type = type.containedType(1);
        }
        if (!Patched.class.equals(type.getRawClass())) {
            throw new IllegalStateException("Cannot determine value type for PatchedSerializers");
        }
        JavaType valueType = type.containedType(0); // T
        BeanProperty valueProperty = SerializerUtils.createSyntheticProperty(valueType);
        JavaType patchType = type.containedType(1); // P
        BeanProperty patchProperty = SerializerUtils.createSyntheticProperty(patchType);
        return new SerializerUtils.ValueAndPatchContentType(new SerializerUtils.ContentType(valueType, valueProperty), new SerializerUtils.ContentType(patchType, patchProperty));
    }

    @AllArgsConstructor
    public static class Serializer extends JsonSerializer<Patched<?, ?>> implements ContextualSerializer {

        private final JsonSerializer<Object> valueSerializer;
        @Nullable
        private final TypeSerializer valueTypeSerializer;
        private final JsonSerializer<Object> patchSerializer;
        @Nullable
        private final TypeSerializer patchTypeSerializer;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Serializer() {
            valueSerializer = patchSerializer = null;
            valueTypeSerializer = patchTypeSerializer = null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ValueAndPatchContentType types = detectTypeParameter(property);
            return new Serializer(
                    SerializerUtils.findValueSerializer(prov, types.value()), SerializerUtils.findTypeSerializer(prov, types.value()),
                    SerializerUtils.findValueSerializer(prov, types.patch()), SerializerUtils.findTypeSerializer(prov, types.patch())
            );
        }

        @Override
        public void serialize(Patched<?, ?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (container.updatedValue() != null) {
                doSerializeValue(container.updatedValue(), gen, serializers, true);
            } else {
                gen.writeStartObject();
                if (container.oldValue() != null) {
                    gen.writeFieldName(FIELD_OLD);
                    doSerializeValue(container.oldValue(), gen, serializers, false);
                }
                if (container.newValue() != null) {
                    gen.writeFieldName(FIELD_NEW);
                    doSerializeValue(container.newValue(), gen, serializers, false);
                }
                gen.writeEndObject();
            }
        }

        private void doSerializeValue(Object value, JsonGenerator gen, SerializerProvider serializers, boolean serializeAsPatch) throws IOException {
            JsonSerializer<Object> serializer = serializeAsPatch ? patchSerializer : valueSerializer;
            TypeSerializer typeSerializer = serializeAsPatch ? patchTypeSerializer : valueTypeSerializer;
            if (typeSerializer != null) {
                serializer.serializeWithType(value, gen, serializers, typeSerializer);
            } else {
                serializer.serialize(value, gen, serializers);
            }
        }
    }

    @AllArgsConstructor
    public static class Deserializer extends JsonDeserializer<Patched<?, ?>> implements ContextualDeserializer {

        private final JavaType valueType;
        private final JavaType patchType;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Deserializer() {
            valueType = patchType = null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ValueAndPatchContentType contentType = detectTypeParameter(property);
            return new Deserializer(contentType.value().type(), contentType.patch().type());
        }

        @Override
        public Patched<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() != JsonToken.START_OBJECT) {
                ctxt.reportWrongTokenException(Patched.class, JsonToken.START_OBJECT, "Patched value expected to be an object");
            }

            // Here, we have two options: either object with $old/$new (created/replaced/deleted), or not (updated).
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
                    Object fieldValue = doDeserializeValue(ctxt, p2, false);
                    switch (fieldName) {
                        case FIELD_OLD -> oldValue = fieldValue;
                        case FIELD_NEW -> newValue = fieldValue;
                        default -> throw new IllegalStateException("Unexpected field: " + p2.currentName() + ", expecting " + FIELD_OLD + " or " + FIELD_NEW);
                    }
                    fieldName = p2.nextFieldName();
                } while (fieldName != null);
                return new Patched<>(oldValue, newValue, null);
            }

            // read as regular value object instead
            JsonParser p3 = buffer.asParser();
            p3.nextToken(); // step to START_OBJECT
            Object value = doDeserializeValue(ctxt, p3, true);
            return Patched.updated(value);
        }

        private Object doDeserializeValue(DeserializationContext ctxt, JsonParser p, boolean deserializeAsPatch) {
            JavaType type = deserializeAsPatch ? patchType : valueType;
            return SerializerUtils.withRootType(
                    type,
                    () -> ctxt.readValue(p, type)
            );
        }
    }
}
