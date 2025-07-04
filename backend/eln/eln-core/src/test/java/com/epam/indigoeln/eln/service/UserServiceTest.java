package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.model.UserRef;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
class UserServiceTest extends BaseTest {

    @Inject
    UserService userService;

    @Test
    @TestSecurity(user = TestHelper.JOHN_USERNAME)
    @JwtSecurity(claims = {@Claim(key = "given_name", value = TestHelper.JOHN_FIRST_NAME), @Claim(key = "family_name", value = TestHelper.JOHN_LAST_NAME)})
    void testGetCurrentUser() {
        userService.getCurrentUser();
    }

    @Test
    @TestSecurity(user = TestHelper.JOHN_USERNAME)
    @JwtSecurity(claims = {@Claim(key = "given_name", value = TestHelper.JOHN_FIRST_NAME), @Claim(key = "family_name", value = TestHelper.JOHN_LAST_NAME)})
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

    @Test
    @TestSecurity(user = TestHelper.JOHN_USERNAME)
    void testSuggestUsers() {
        List<UserRef> all = userClient.suggestUsers(null);
        assertThat(all).map(UserRef::getDisplayName).containsExactly(TestHelper.ADMIN_DISPLAY_NAME, TestHelper.BART_DISPLAY_NAME, TestHelper.JOHN_DISPLAY_NAME, TestHelper.LISA_DISPLAY_NAME, TestHelper.WILLOW_DISPLAY_NAME);
        List<UserRef> filtered = userClient.suggestUsers("l");
        assertThat(filtered).map(UserRef::getDisplayName).containsExactly(TestHelper.LISA_DISPLAY_NAME);
    }

    @Test
    @SneakyThrows
    @TestSecurity(user = TestHelper.JOHN_USERNAME)
    void testGetUserPicture() {
        List<UserRef> all = userClient.suggestUsers(null);
        ResponseWithHeaders response = userClient.getUserPictureClient(all.getFirst().getId(), null);
        Files.write(Paths.get("user.png"), response.getContent().readAllBytes());
    }
}
