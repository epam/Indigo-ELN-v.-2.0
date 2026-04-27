package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.quarkus.cachecontrol.CachedEndpoints;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Provider
public class CacheControlFilter implements ContainerResponseFilter {

    private static final RuntimeDelegate.HeaderDelegate<CacheControl> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);

    @jakarta.ws.rs.core.Context
    ResourceInfo resourceInfo;

    @Inject
    CachedEndpoints cachedEndpoints;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String key = resourceInfo.getResourceClass().getName() + "#" + resourceInfo.getResourceMethod().getName();
        Integer maxAge = cachedEndpoints.getMaxAge(key);
        if (maxAge != null) {
            CacheControl cc = new CacheControl();
            cc.setMaxAge(maxAge);
            responseContext.getHeaders().putSingle("Cache-Control", HEADER_DELEGATE.toString(cc));
        }
    }
}
