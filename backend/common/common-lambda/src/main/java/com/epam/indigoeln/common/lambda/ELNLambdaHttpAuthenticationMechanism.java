package com.epam.indigoeln.common.lambda;

import io.quarkus.amazon.lambda.http.CustomPrincipal;
import io.quarkus.amazon.lambda.http.LambdaHttpAuthenticationMechanism;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.Map;

@Alternative
@ApplicationScoped
public class ELNLambdaHttpAuthenticationMechanism extends LambdaHttpAuthenticationMechanism {

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext routingContext, IdentityProviderManager identityProviderManager) {
        System.out.println("!!!0");
        return super.authenticate(routingContext, identityProviderManager)
                .map(identity -> {
                    System.out.println("!!!1 identity: " + identity);
                    if (identity == null) {
                        String authorization = routingContext.request().headers().get("X-Integration-Test-Authorization");
                        System.out.println("!!!2 authorization: " + authorization);
                        if (authorization != null) {
                            System.out.println("!!!3");
                            return QuarkusSecurityIdentity.builder()
                                    .setPrincipal(new CustomPrincipal(authorization, Map.of()))
                                    .build();
                        }
                    }
                    return null;
                });
    }
}
