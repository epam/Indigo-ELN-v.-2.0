package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.api.Cached;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;

@Slf4j
@Provider
public class CacheControlFilter implements ContainerResponseFilter {

    private static final RuntimeDelegate.HeaderDelegate<CacheControl> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);

    @jakarta.ws.rs.core.Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        log.info("!!! filter start");
        Cached cachedOld = MethodUtils.getAnnotation(resourceInfo.getResourceMethod(), Cached.class, true, false);
        Cached cached = findAnnotation(resourceInfo.getResourceMethod(), Cached.class);
        if (cached != null) {
            CacheControl cc = new CacheControl();
            Duration duration = Duration.of(cached.interval(), cached.unit());
            cc.setMaxAge((int) duration.getSeconds());
            responseContext.getHeaders().putSingle("X-Cache-Control", HEADER_DELEGATE.toString(cc));
            responseContext.getHeaders().putSingle("Cache-Control", HEADER_DELEGATE.toString(cc));
        }
    }

    @Nullable
    private <T extends Annotation> T findAnnotation(Method method, Class<T> annotationClass) {
        T found = method.getAnnotation(annotationClass);
        log.info("!!! findAnnotation: {} on method {}: {}", annotationClass.getName(), method, found);
        if (found != null) {
            return found;
        }
        for (Class<?> interfaceClass : method.getDeclaringClass().getInterfaces()) {
            try {
                Method interfaceMethod = interfaceClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                found = interfaceMethod.getAnnotation(annotationClass);
                log.info("!!! findAnnotation: {} on method {} on interface {}: {}", annotationClass.getName(), interfaceMethod, interfaceClass.getName(), found);
                if (found != null) {
                    return found;
                }
            } catch (NoSuchMethodException ignore) {
            }
        }
        return null;
    }
}
