package com.epam.indigoeln.aws;

import lombok.Getter;
import software.amazon.awscdk.services.cognito.*;
import software.constructs.Construct;

import java.util.List;

public class CognitoStack {

    @Getter
    private final UserPool userPool;
    @Getter
    private final UserPoolClient userPoolClient;

    public CognitoStack(Construct scope, Props props) {
        userPool = UserPool.Builder.create(scope, "user-pool")
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(6)
                        .requireDigits(false)
                        .requireLowercase(false)
                        .requireUppercase(false)
                        .requireSymbols(false)
                        .build())
                .build();
        UserPoolDomain.Builder.create(scope, "user-pool-domain")
                .userPool(userPool)
                .cognitoDomain(CognitoDomainOptions.builder().domainPrefix("indigoeln-" + props.envName()).build())
                .build();

        createUser(scope, userPool, "admin", "Administrator", "Administrator");
        userPoolClient = userPool.addClient("user-pool-eln-client", UserPoolClientOptions.builder()
                .userPoolClientName("eln-client")
                .generateSecret(false)
                .oAuth(OAuthSettings.builder()
                        .callbackUrls(List.of(
                                "https://" + props.domainName() + "/",
                                "https://" + props.domainName() + "/frontend2/",
                                "http://localhost:5173/frontend2/",
                                "http://localhost:4200"
                        ))
                        .build()
                )
                .build());
    }

    private void createUser(Construct scope, UserPool userPool, String username, String givenName, String familyName) {
        CfnUserPoolUser.Builder.create(scope, "user-" + username)
                .userPoolId(userPool.getUserPoolId())
                .username(username)
                .userAttributes(List.of(
                        CfnUserPoolUser.AttributeTypeProperty.builder().name("given_name").value(givenName).build(),
                        CfnUserPoolUser.AttributeTypeProperty.builder().name("family_name").value(familyName).build()
                ))
                .build();
    }

    public record Props(
            String domainName,
            String envName
    ) {}
}
