package com.epam.indigoeln.common.lambda;

import io.quarkus.amazon.lambda.http.LambdaAuthenticationRequest;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alternative
@ApplicationScoped
public class ELNIdentityProvider implements IdentityProvider<LambdaAuthenticationRequest> {

    private static final Logger log = LoggerFactory.getLogger(ELNIdentityProvider.class);

    @Override
    public Class<LambdaAuthenticationRequest> getRequestType() {
        return LambdaAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(LambdaAuthenticationRequest request, AuthenticationRequestContext context) {
        log.info("!!! request.event: {}", request.getEvent());
        if(1==1)throw new RuntimeException("!!! request.event: " + request.getEvent());
        return Uni.createFrom().nothing();
    }
}
