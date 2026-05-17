package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.quarkus.cachecontrol.CachedEndpoints;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.jboss.resteasy.reactive.server.SimpleResourceInfo;

public class CacheControlFilter {

    private static final RuntimeDelegate.HeaderDelegate<CacheControl> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);

    @Inject
    CachedEndpoints cachedEndpoints;

    @ServerResponseFilter
    public void filter(ContainerResponseContext responseContext, SimpleResourceInfo resourceInfo) {
        if (resourceInfo.getResourceClass() == null) {
            return;
        }
        String key = resourceInfo.getResourceClass().getName() + "#" + resourceInfo.getMethodName();
        Integer maxAge = cachedEndpoints.getMaxAge(key);
        if (maxAge != null) {
            CacheControl cc = new CacheControl();
            cc.setMaxAge(maxAge);
            responseContext.getHeaders().putSingle("Cache-Control", HEADER_DELEGATE.toString(cc));
        }
    }
}
