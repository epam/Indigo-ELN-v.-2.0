package com.epam.indigoeln.common.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.quarkus.amazon.lambda.http.CognitoPrincipal;
import io.quarkus.amazon.lambda.http.LambdaIdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdentityProviderImpl implements LambdaIdentityProvider {

    @Override
    public SecurityIdentity authenticate(APIGatewayV2HTTPEvent event) {
        CognitoPrincipal principal = new CognitoPrincipal(event.getRequestContext().getAuthorizer().getJwt());
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
        builder.setPrincipal(principal);
        for (String group : principal.getGroups()) {
            builder.addRole(group);
        }
        return builder.build();
    }
}
