package com.epam.indigoeln.common.config;

import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.core.CurrentRequestManager;

import java.security.Principal;

@Slf4j
@RequestScoped
public class UserInfo {

    public static final String X_TEST_AUTHORIZATION = "X-Test-Authorization";

    private final @Nullable JsonWebToken jwt;
    private final @Nullable String username;

    @Inject
    UserInfo(SecurityIdentity identity) {
        Principal principal = identity.getPrincipal();
        jwt = principal instanceof JsonWebToken ? (JsonWebToken) principal : null;
        String user = null;
        if (CurrentRequestManager.get() != null && ConfigUtils.isProfileActive("devtest")) {
            user = CurrentRequestManager.get().getHttpHeaders().getRequestHeaders().getFirst(X_TEST_AUTHORIZATION);
        }
        if (jwt != null) {
            if (user == null) {
                user = jwt.getClaim(Claims.preferred_username);
            }
            if (user == null) {
                user = jwt.getClaim("username");
            }
            if (user == null) {
                user = identity.getPrincipal().getName();
            }
        }
        username = user;
    }

    public String getUserName() {
        if (username == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return username;
    }

    public @Nullable String getLastName() {
        return jwt != null ? jwt.getClaim(Claims.family_name) : null;
    }

    public @Nullable String getFirstName() {
        return jwt != null ? jwt.getClaim(Claims.given_name) : null;
    }
}
