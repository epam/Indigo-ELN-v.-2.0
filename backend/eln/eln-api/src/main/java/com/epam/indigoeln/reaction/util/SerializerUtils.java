package com.epam.indigoeln.reaction.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import org.jspecify.annotations.Nullable;

import java.lang.ScopedValue;

public class SerializerUtils {

    // only used for testing, where we want to serialize generic value directly;
    // in production code, generic value will typically be a property of some object, and type will be read from that property
    public static final ThreadLocal<@Nullable JavaType> ROOT_TYPE = new ThreadLocal<>();

    public static JavaType findContextType(@Nullable BeanProperty property) {
        JavaType type = null;
        if (property != null) {
            type = property.getType();
        } else if (ROOT_TYPE.get() != null) {
            type = ROOT_TYPE.get();
        }
        if (type == null) {
            throw new IllegalStateException("Cannot detect context type");
        }
        return type;
    }

    // create a synthetic property for the content type to preserve generic context
    public static BeanProperty createSyntheticProperty(JavaType contentType) {
        return new BeanProperty.Std(
                PropertyName.construct("value"),
                contentType,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );
    }

    public static JsonSerializer<Object> findValueSerializer(SerializerProvider prov, ContentType contentTypes) throws JsonMappingException {
        JsonSerializer<Object> serializer = prov.findValueSerializer(contentTypes.type, contentTypes.property);
        if (serializer instanceof ResolvableSerializer rs) {
            rs.resolve(prov);
        }
        return serializer;
    }

    public static JsonDeserializer<Object> findValueDeserializer(DeserializationContext ctxt, SerializerUtils.ContentType contentTypes) throws JsonMappingException {
        JsonDeserializer<Object> valueDeserializer1 = ctxt.findContextualValueDeserializer(contentTypes.type, contentTypes.property);
        if (valueDeserializer1 instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) valueDeserializer1).resolve(ctxt);
        }
        return valueDeserializer1;
    }

    public record ContentType (
            JavaType type,
            BeanProperty property
    ) {
    }
}
