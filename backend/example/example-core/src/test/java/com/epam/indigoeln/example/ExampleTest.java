package com.epam.indigoeln.example;

import com.epam.indigoeln.example.client.ExampleClient;
import com.epam.indigoeln.example.controller.ExampleResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.MediaType;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.*;

import java.net.URI;

import static io.restassured.RestAssured.given;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExampleTest {

    @TestHTTPResource
    @TestHTTPEndpoint(ExampleResource.class)
    URI serverURL;

    ExampleClient client;

    @BeforeEach
    void setup() {
        client = RestClientBuilder.newBuilder().baseUri(serverURL.resolve("/")).build(ExampleClient.class);
    }

//    @Test
//    void testUnauthorized() {
//        Assertions.assertThatThrownBy(() -> client.getInfo())
//                .isInstanceOfSatisfying(WebClientApplicationException.class, e -> {
//                    Assertions.assertThat(e.getResponse().getStatus()).isEqualTo(401);
//                });
//    }

    @Test
    @TestSecurity(user = "john")
    @JwtSecurity(claims = {@Claim(key = "given_name", value = "John"), @Claim(key = "family_name", value = "Doe")})
    void testInfoAuthorized() {
        var info = client.getInfo();
        Assertions.assertThat(info.get("principal.name")).isEqualTo("john");
    }

    @Test
    void testParameterValidation() {
        Assertions.assertThatThrownBy(() -> client.sum(-1, 1))
                .isInstanceOfSatisfying(ClientWebApplicationException.class, e -> {
                    Assertions.assertThat(e.getResponse().getStatus()).isEqualTo(400);
                });
    }

    @Test
    void testResultValidation() {
        Assertions.assertThatThrownBy(() -> client.sum(1, -3))
                .isInstanceOfSatisfying(ClientWebApplicationException.class, e -> {
                    Assertions.assertThat(e.getResponse().getStatus()).isEqualTo(500);
                });
    }

    @Test
    void testSwagger() {
        given()
                .when().get("/openapi/example")
                        .then().statusCode(200).contentType("application/yaml");
        given()
                .when().get("/swagger/example")
                .then().statusCode(200).contentType(MediaType.TEXT_HTML);
    }
}
