package com.epam.indigoeln.eln.config;

import io.quarkus.security.spi.runtime.*;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import org.jboss.logging.Logger;
import org.jspecify.annotations.Nullable;

public class SecurityEventObserver {

    private static final Logger LOG = Logger.getLogger(SecurityEventObserver.class.getName());

    void observeAuthenticationSuccess(@ObservesAsync AuthenticationSuccessEvent event) {    
        LOG.debugf("User '%s' has authenticated successfully", event.getSecurityIdentity().getPrincipal().getName());
    }

    void observeAuthenticationFailure(@ObservesAsync AuthenticationFailureEvent event) {
        RoutingContext routingContext = (RoutingContext) event.getEventProperties().get(RoutingContext.class.getName());
        LOG.debugf("Authentication failed, request path: '%s'", routingContext.request().path());
    }

    void observeAuthorizationSuccess(@ObservesAsync AuthorizationSuccessEvent event) {
        String principalName = getPrincipalName(event);
        if (principalName != null) {
            LOG.debugf("User '%s' has been authorized successfully", principalName);
        }
    }

    void observeAuthorizationFailure(@Observes AuthorizationFailureEvent event) {
        LOG.debugf(event.getAuthorizationFailure(), "User '%s' authorization failed", event.getSecurityIdentity().getPrincipal().getName());
    }

    private static @Nullable String getPrincipalName(SecurityEvent event) {
        if (event.getSecurityIdentity() != null) {
            return event.getSecurityIdentity().getPrincipal().getName();
        }
        return null;
    }

}
