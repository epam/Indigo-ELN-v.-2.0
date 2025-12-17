package com.epam.indigoeln.signature;

import com.epam.indigoeln.signature.api.SignatureAPI;
import com.epam.indigoeln.signature.client.SignatureClient;
import com.epam.indigoeln.signature.controller.SignatureResource;
import com.epam.indigoeln.signature.model.*;
import com.epam.indigoeln.signature.service.UserService;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "john")
@JwtSecurity(claims = {@Claim(key = "given_name", value = "John"), @Claim(key = "family_name", value = "Doe")})
class SignatureTest extends BaseTest {

    @TestHTTPResource
    @TestHTTPEndpoint(SignatureResource.class)
    URI serverURL;

    SignatureClient client;
    @Inject UserService userService;

    Integer templateID;
    Integer documentID;

    @BeforeEach
    void setup() {
        client = buildClient(SignatureClient.class);
        userService.getOrCreateUser("willow", "Willow", "Johnson");
    }

    @Test
    void testCreateTemplateValidation() {
        assertThatThrownBy(() -> client.createTemplate(new TemplateRequest(null, List.of())))
                .isInstanceOfSatisfying(ClientWebApplicationException.class, e -> {
                    assertThat(e.getResponse().getStatus()).isEqualTo(400);
                });
    }

    @Test
    @Order(100)
    void testCreateTemplate() {
        Template template = client.createTemplate(new TemplateRequest("test-" + ZonedDateTime.now(), List.of(
                new TemplateSignatureBlockRequest(null, Reason.AUTHOR),
                new TemplateSignatureBlockRequest("willow", Reason.WITNESS)
        )));
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).startsWith("test-");
        assertThat(template.getAuthor()).isEqualTo("John Doe");
        assertThat(template.getCreatedDate()).isNotNull();
        assertThat(template.getLastModifiedDate()).isNotNull();
        assertThat(template.getSignatureBlocks()).hasSize(2)
                .first().satisfies(block -> {
                    assertThat(block.getUsername()).isNull();
                    assertThat(block.getReason()).isEqualTo(Reason.AUTHOR);
                });
        assertThat(template.getSignatureBlocks())
                .last().satisfies(block -> {
                    assertThat(block.getUsername()).isEqualTo("Willow Johnson");
                    assertThat(block.getReason()).isEqualTo(Reason.WITNESS);
        });
        templateID = template.getId();
    }

    @Test
    @Order(200)
    void testUploadDocument() throws Exception {
        assumeThat(templateID).isNotNull();
        Document document = client.uploadDocument(
                new SignatureAPI.FileUploadForm(templateID, "document.pdf", getClass().getResourceAsStream("/document.pdf").readAllBytes())
        );
        documentID = document.getId();
        assertThat(document.getId()).isNotNull();
        assertThat(document.getName()).isEqualTo("document.pdf");
        assertThat(document.getStatus()).isEqualTo(Status.SUBMITTED);
        assertThat(document.getCreatedDate()).isNotNull();
        assertThat(document.getLastModifiedDate()).isNotNull();
        assertThat(document.getAuthor()).isEqualTo("John Doe");
        assertThat(document.getSignatureBlocks()).hasSize(2)
                .first().satisfies(block -> {
                    assertThat(block.getUser()).isEqualTo("John Doe");
                    assertThat(block.getReason()).isEqualTo(Reason.AUTHOR);
                    assertThat(block.getActionDate()).isNull();
                    assertThat(block.getStatus()).isEqualTo(SignatureStatus.WAITING);
                    assertThat(block.getComment()).isNull();
                });
        assertThat(document.getSignatureBlocks())
                .last().satisfies(block -> {
                    assertThat(block.getUser()).isEqualTo("Willow Johnson");
                    assertThat(block.getReason()).isEqualTo(Reason.WITNESS);
                    assertThat(block.getStatus()).isEqualTo(SignatureStatus.WAITING);
                });
    }

    @Test
    @Order(300)
    void testSign() throws Exception {
        assumeThat(documentID).isNotNull();
        Document document = client.signDocument(
                documentID,
                new SignatureAPI.SignForm(getClass().getResourceAsStream("/keystore.p12").readAllBytes(), "1234")
        );
        assertThat(document.getStatus()).isEqualTo(Status.SIGNING);
        assertThat(document.getLastModifiedDate()).isNotEqualTo(document.getCreatedDate());
        assertThat(document.getSignatureBlocks()).first().satisfies(block -> {
            assertThat(block.getStatus()).isEqualTo(SignatureStatus.SIGNED);
            assertThat(block.getActionDate()).isNotNull();
            assertThat(block.getUser()).isEqualTo("John Doe");
        });
    }

    @Test
    @Order(400)
    @TestSecurity(user = "willow")
    @JwtSecurity(claims = {@Claim(key = "given_name", value = "Willow"), @Claim(key = "family_name", value = "Johnson")})
    void testReject() {
        assumeThat(documentID).isNotNull();
        Document document = client.rejectDocument(documentID);
        assertThat(document.getStatus()).isEqualTo(Status.REJECTED);
        assertThat(document.getLastModifiedDate()).isNotEqualTo(document.getCreatedDate());
        assertThat(document.getSignatureBlocks()).last().satisfies(block -> {
            assertThat(block.getStatus()).isEqualTo(SignatureStatus.REJECTED);
            assertThat(block.getActionDate()).isNotNull();
            assertThat(block.getUser()).isEqualTo("Willow Johnson");
        });
    }

    @Test
    @Order(500)
    void testGetDocuments() {
        assumeThat(documentID).isNotNull();
        List<Document> documents = client.getDocuments();
        assertThat(documents).filteredOn(d -> d.getId().equals(documentID)).hasSize(1).first().satisfies(document -> {
            assertThat(document.getStatus()).isEqualTo(Status.REJECTED);
            assertThat(document.getSignatureBlocks()).hasSize(2);
        });
    }

    @Test
    @Order(600)
    void testDownloadDocument() throws Exception {
        assumeThat(documentID).isNotNull();
        Response content = client.downloadDocument(documentID);
        try (FileOutputStream fos = new FileOutputStream("downloaded.pdf")) {
            fos.write(content.readEntity(byte[].class));
        }
    }
}
