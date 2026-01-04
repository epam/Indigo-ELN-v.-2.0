package com.epam.indigoeln.reaction.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Callable;

public class SerializerUtils {

    // !!! switch to ScopedValue when upgraded to Java 25

    // only used for testing, where we want to serialize generic value directly;
    // in production code, generic value will typically be a property of some object, and type will be read from that property
    private static final ThreadLocal<@Nullable JavaType> ROOT_TYPE = new ThreadLocal<>();

    @SneakyThrows
    public static <R> R withRootTypeForTesting(JavaType type, Callable<R> block) {
        JavaType oldType = ROOT_TYPE.get();
        try {
            ROOT_TYPE.set(type);
            return block.call();
        } finally {
            if (oldType != null) {
                ROOT_TYPE.set(oldType);
            } else  {
                ROOT_TYPE.remove();
            }
        }
    }

    public static void withRootTypeForTesting(JavaType type, ThrowingRunnable block) {
        withRootTypeForTesting(type, block.asCallable());
    }

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

    @Nullable
    public static TypeSerializer findTypeSerializer(SerializerProvider prov, ContentType contentTypes) throws JsonMappingException {
        return prov.findTypeSerializer(contentTypes.type);
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
