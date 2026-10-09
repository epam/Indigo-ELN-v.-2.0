package com.epam.indigoeln.sampleregistration.config;

import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class STRCodeCompoundConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(STRCodeCompound.class)) {
            //noinspection unchecked
            return (ParamConverter<T>) new Impl();
        }
        return null;
    }

    static class Impl implements ParamConverter<STRCodeCompound> {

        @Override
        @Nullable
        public STRCodeCompound fromString(@Nullable String value) {
            return value != null ? STRCodeCompound.parse(value) : null;
        }

        @Override
        @Nullable
        public String toString(@Nullable STRCodeCompound value) {
            return value != null ? value.toString() : null;
        }
    }
}
