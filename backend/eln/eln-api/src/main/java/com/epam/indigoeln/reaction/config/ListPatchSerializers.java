package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListPatchSerializers {

    public static final String UNCHANGED = "$unchanged";
    private static final char INDEX_SEPARATOR = '>';

    private static SerializerUtils.ContentType detectTypeParameter(@Nullable BeanProperty property, TypeFactory typeFactory) {
        JavaType type = SerializerUtils.findContextType(property); // type is ListPatch<T, P>
        if (!type.getRawClass().equals(ListPatch.class)) {
            throw new IllegalStateException("Cannot determine value type for ListPatchSerializers");
        }
        JavaType valueType = type.containedType(0); // T
        JavaType patchType = type.containedType(1); // P
        JavaType resultType = typeFactory.constructParametricType(Patched.class, valueType, patchType); // Patched<T, P>
        BeanProperty resultProperty = SerializerUtils.createSyntheticProperty(resultType);
        return new SerializerUtils.ContentType(resultType, resultProperty);
    }

    public static String keyToString(ListPatch.Item<?, ?> item) {
        if (item.newIndex() != null && item.newIndex().equals(item.oldIndex())) {
            return item.newIndex().toString();
        }
        StringBuilder str = new StringBuilder();
        if (item.oldIndex() != null) {
            str.append(item.oldIndex());
        }
        str.append(INDEX_SEPARATOR);
        if (item.newIndex() != null) {
            str.append(item.newIndex());
        }
        return str.toString();
    }

    public static Pair<@Nullable Integer, @Nullable Integer> parseKey(String str) {
        int p = str.indexOf(INDEX_SEPARATOR);
        if (p == -1) {
            Integer index = Integer.valueOf(str);
            return Pair.of(index, index);
        }
        Integer from = p > 0 ? Integer.valueOf(str.substring(0, p)) : null;
        Integer to = p < str.length() - 1 ? Integer.valueOf(str.substring(p + 1)) : null;
        return Pair.of(from, to);
    }

    public static Pair<@Nullable Integer, @Nullable Integer> reverseKey(Pair<@Nullable Integer, @Nullable Integer> key) {
        return Pair.of(key.b(), key.a());
    }

    @AllArgsConstructor
    public static class Serializer extends JsonSerializer<ListPatch<?, ?>> implements ContextualSerializer {

        private final JsonSerializer<Object> valueSerializer;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Serializer() {
            valueSerializer = null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ContentType contentTypes = detectTypeParameter(property, prov.getTypeFactory());
            return new Serializer(SerializerUtils.findValueSerializer(prov, contentTypes));
        }

        @Override
        public void serialize(ListPatch<?, ?> container, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            for (ListPatch.Item<?, ?> item : container.getItems()) {
                gen.writeFieldName(keyToString(item));
                if (item.value() != null) {
                    valueSerializer.serialize(item.value(), gen, serializers);
                } else {
                    gen.writeString(UNCHANGED);
                }
            }

            gen.writeEndObject();
        }
    }

    @AllArgsConstructor
    public static class Deserializer extends JsonDeserializer<ListPatch<?, ?>> implements ContextualDeserializer {

        private final JsonDeserializer<Object> valueDeserializer;

        @SuppressWarnings({"DataFlowIssue", "unused"})
        public Deserializer() {
            valueDeserializer = null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            SerializerUtils.ContentType contentTypes = detectTypeParameter(property, ctxt.getTypeFactory());
            JsonDeserializer<Object> valueDeserializer1 = SerializerUtils.findValueDeserializer(ctxt, contentTypes);
            return new Deserializer(valueDeserializer1);
        }

        @Override
        public ListPatch<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() != JsonToken.START_OBJECT) {
                ctxt.reportWrongTokenException(this, JsonToken.START_OBJECT, "Object expected");
            }

            List<ListPatch.Item<?, ?>> items = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_OBJECT) { // step to field key or end object
                Pair<@Nullable Integer, @Nullable Integer> index = parseKey(p.currentName());
                p.nextToken(); // step to field value

                Object value = p.currentToken() == JsonToken.VALUE_STRING && UNCHANGED.equals(p.getValueAsString())
                        ? null
                        : valueDeserializer.deserialize(p, ctxt);

                //noinspection rawtypes,unchecked
                items.add(new ListPatch.Item<Object, Object>(index.a(), index.b(), (Patched) value));
            }

            //noinspection rawtypes,unchecked
            return new ListPatch(items);
        }
    }
}
