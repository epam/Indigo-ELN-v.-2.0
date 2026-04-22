package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.api.Cached;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.IOException;
import java.time.Duration;

@Provider
public class CacheControlFilter implements ContainerResponseFilter {

    @jakarta.ws.rs.core.Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Cached cached = MethodUtils.getAnnotation(resourceInfo.getResourceMethod(), Cached.class, true, false);
        if (cached != null) {
            CacheControl cc = new CacheControl();
            Duration duration = Duration.of(cached.interval(), cached.unit());
            cc.setMaxAge((int) duration.getSeconds());
            responseContext.getHeaders().putSingle("X-Cache-Control", cc.toString());
            responseContext.getHeaders().putSingle("Cache-Control", cc.toString());
        }
    }
}
