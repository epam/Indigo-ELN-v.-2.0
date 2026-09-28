package com.epam.indigoeln.test;

import feign.Contract;
import feign.MethodMetadata;
import jakarta.ws.rs.BeanParam;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

/**
 * feign-jaxrs3's contract maps a @BeanParam's @QueryParam fields onto the same
 * argument index under multiple template names, but Feign has no expander for
 * "one index, many names" — it resolves by calling toString() on the whole bean
 * and substitutes that into every one of those placeholders. This rewrites such
 * parameters to go through a QueryMapEncoder instead, which Feign does support.
 */
@RequiredArgsConstructor
public class BeanParamContract implements Contract {

    private final Contract delegate;

    @Override
    public List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType) {
        List<MethodMetadata> methods = delegate.parseAndValidateMetadata(targetType);
        for (MethodMetadata metadata : methods) {
            Parameter[] parameters = metadata.method().getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(BeanParam.class)) {
                    Collection<String> names = metadata.indexToName().get(i);
                    if (names != null) {
                        for (String name : names) {
                            metadata.template().query(name, (String[]) null);
                        }
                        metadata.indexToName().remove(i);
                    }
                    metadata.queryMapIndex(i);
                }
            }
        }
        return methods;
    }
}
