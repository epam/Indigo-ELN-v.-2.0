package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.cognito.*;
import software.constructs.Construct;

import java.util.List;

public class CognitoStack extends NestedStack {

    @Getter
    private final UserPool userPool;
    @Getter
    private final UserPoolClient userPoolClient;

    public CognitoStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        userPool = UserPool.Builder.create(this, "user-pool")
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(6)
                        .requireDigits(false)
                        .requireLowercase(false)
                        .requireUppercase(false)
                        .requireSymbols(false)
                        .build())
                .build();
        UserPoolDomain.Builder.create(this, "user-pool-domain")
                .userPool(userPool)
                .cognitoDomain(CognitoDomainOptions.builder().domainPrefix("indigoeln-" + props.getEnvName()).build())
                .build();

        createUser(userPool, "alice", "Alice", "Smith");
        createUser(userPool, "bob", "Bob", "Johnson");
        createUser(userPool, "charlie", "Charlie", "Williams");
        userPoolClient = userPool.addClient("user-pool-eln-client", UserPoolClientOptions.builder()
                .userPoolClientName("eln-client")
                .generateSecret(false)
                .oAuth(OAuthSettings.builder()
                        .callbackUrls(List.of(
                                "https://" + props.getDomainName() + "/",
                                "http://localhost:5173/",
                                "http://localhost:4200"
                        ))
                        .build()
                )
                .build());
    }

    private void createUser(UserPool userPool, String username, String givenName, String familyName) {
        CfnUserPoolUser.Builder.create(this, "user-" + username)
                .userPoolId(userPool.getUserPoolId())
                .username(username)
                .userAttributes(List.of(
                        CfnUserPoolUser.AttributeTypeProperty.builder().name("given_name").value(givenName).build(),
                        CfnUserPoolUser.AttributeTypeProperty.builder().name("family_name").value(familyName).build()
                ))
                .build();
    }

    @Value
    public static class Props implements NestedStackProps {

        String domainName;
        String envName;
    }
}
