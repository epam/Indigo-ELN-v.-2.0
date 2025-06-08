package com.epam.indigoeln.signature.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    @TestSecurity(user = "john")
    @JwtSecurity(claims = {@Claim(key = "given_name", value = "John"), @Claim(key = "family_name", value = "Doe")})
    void testGetCurrentUser() {
        userService.getCurrentUser();
    }

    @Test
    @TestSecurity(user = "john")
    @JwtSecurity(claims = {@Claim(key = "given_name", value = "John"), @Claim(key = "family_name", value = "Doe")})
    void testGetCurrentUserLambdaJWT() throws Exception {
        userService.getCurrentUser();
//        Map<String, String> claims = new LinkedHashMap<>();
//        claims.put("sub", "c498d4b8-5061-70fe-686c-596a0e41bba2");
//        claims.put("iss", "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_GnMjXfy1G");
//        claims.put("version", "2");
//        claims.put("client_id", "7733m4vdkml83fjq7h6mlq85fl");
//        claims.put("origin_jti", "6971a3a7-8dd0-4b3d-84e7-ebe416c015c3");
//        claims.put("event_id", "b8944927-8731-4baa-be5b-a91a6ac623c1");
//        claims.put("token_use", "access");
//        claims.put("scope", "aws.cognito.signin.user.admin phone openid profile email");
//        claims.put("auth_time", "1740940661");
//        claims.put("exp", "1740951361");
//        claims.put("iat", "1740947761");
//        claims.put("jti", "8a38ac6e-4294-4391-9d24-8e3e5be3b29c");
//        claims.put("username", "alice");
//        JsonWebToken jwt = new CognitoPrincipal(new APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT(claims, List.of("aws.cognito.signin.user.admin", "phone", "openid", "profile", "email")));
//        userService.getCurrentUser(jwt);
    }
}
