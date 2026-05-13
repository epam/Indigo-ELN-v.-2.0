package com.epam.indigoeln.signature;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.api.ELNInternalClient;
import com.epam.indigoeln.signature.api.SignatureAdminClient;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.signature.model.*;
import com.epam.indigoeln.test.APICallException;
import com.epam.indigoeln.test.BaseTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;


@QuarkusTest
@ConnectWireMock
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "john")
@JwtSecurity(claims = {@Claim(key = "given_name", value = "John"), @Claim(key = "family_name", value = "Doe")})
class SignatureServiceTest extends BaseTest {

    WireMock wireMock;

    SignatureClient signatureClient;
    SignatureAdminClient signatureAdminClient;
    ELNInternalClient elnInternalClient;

    UUID templateID;
    UUID documentID;

    UserRef johnUserRef;
    UserRef willowUserRef;

    @BeforeAll
    void setup() {
        signatureClient = buildClient(SignatureClient.class);
        signatureAdminClient = buildClient(SignatureAdminClient.class);
        signatureAdminClient.cleanupDatabase();
        elnInternalClient = buildClient(ELNInternalClient.class);

        johnUserRef = signatureAdminClient.getOrCreateUser("john", "John", "Doe");
        willowUserRef = signatureAdminClient.getOrCreateUser("willow", "Willow", "Johnson");

        if (!integrationTest) {
            wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/internalapi/eln/signatureUpdated")).willReturn(WireMock.aResponse()
                    .withStatus(Response.Status.NO_CONTENT.getStatusCode())));
        }
    }

    @Test
    void testCreateTemplateValidation() {
        assertThatThrownBy(() -> signatureClient.createTemplate(new SignatureTemplateRequest(null, List.of())))
                .isInstanceOfSatisfying(APICallException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(400);
                });
    }

    @Test
    @Order(100)
    void testCreateTemplate() {
        SignatureTemplateDetailsDTO template = signatureClient.createTemplate(new SignatureTemplateRequest("testCreateTemplate", List.of(
                new SignatureTemplateBlock(null, SignatureReason.AUTHOR),
                new SignatureTemplateBlock(willowUserRef, SignatureReason.WITNESS)
        )));
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).isEqualTo("testCreateTemplate");
        assertThat(template.getCreatedBy()).isEqualTo(johnUserRef);
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getModifiedBy()).isEqualTo(johnUserRef);
        assertThat(template.getModifiedAt()).isNotNull();
        assertThat(template.getBlocks()).hasSize(2)
                .first().satisfies(block -> {
                    assertThat(block.getUser()).isNull();
                    assertThat(block.getReason()).isEqualTo(SignatureReason.AUTHOR);
                });
        assertThat(template.getBlocks())
                .last().satisfies(block -> {
                    assertThat(block.getUser()).isEqualTo(willowUserRef);
                    assertThat(block.getReason()).isEqualTo(SignatureReason.WITNESS);
        });
        templateID = template.getId();
    }

    @Test
    @Order(110)
    void testListTemplates() {
        assertThat(signatureClient.getTemplates()).singleElement().satisfies(template -> {
            assertThat(template.getId()).isEqualTo(templateID);
            assertThat(template.getName()).isEqualTo("testCreateTemplate");
        });
    }

    @Test
    @Order(200)
    void testUploadDocument(@TempDir Path tempDir) throws Exception {
        assumeThat(templateID).isNotNull();
        Path file = tempDir.resolve("document.pdf");
        Files.write(file, ModelUtil.loadResource(getClass(), "/document.pdf"));
        DocumentDTO document = signatureClient.uploadDocumentClient("document.pdf", templateID, file.toFile());
        documentID = document.getId();
        assertThat(document.getId()).isNotNull();
        assertThat(document.getName()).isEqualTo("document.pdf");
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        assertThat(document.getCreatedDate()).isNotNull();
        assertThat(document.getLastModifiedDate()).isNotNull();
        assertThat(document.getAuthor()).isEqualTo(johnUserRef);
        assertThat(document.getSignatures()).hasSize(2)
                .first().satisfies(block -> {
                    assertThat(block.getUser()).isEqualTo(johnUserRef);
                    assertThat(block.getReason()).isEqualTo(SignatureReason.AUTHOR);
                    assertThat(block.getActionDate()).isNull();
                    assertThat(block.getStatus()).isEqualTo(SignatureStatus.WAITING);
                    assertThat(block.getComment()).isNull();
                });
        assertThat(document.getSignatures())
                .last().satisfies(block -> {
                    assertThat(block.getUser()).isEqualTo(willowUserRef);
                    assertThat(block.getReason()).isEqualTo(SignatureReason.WITNESS);
                    assertThat(block.getStatus()).isEqualTo(SignatureStatus.WAITING);
                });
    }

    @Test
    @Order(300)
    void testSign() throws Exception {
        assumeThat(!integrationTest); // no matching document in ELN
        assumeThat(documentID).isNotNull();
        DocumentDTO document = signatureClient.signDocument(documentID);
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.SIGNING);
        assertThat(document.getLastModifiedDate()).isNotEqualTo(document.getCreatedDate());
        assertThat(document.getSignatures()).first().satisfies(block -> {
            assertThat(block.getStatus()).isEqualTo(SignatureStatus.APPROVED);
            assertThat(block.getActionDate()).isNotNull();
            assertThat(block.getUser()).isEqualTo(johnUserRef);
        });
    }

    @Test
    @Order(400)
    @TestSecurity(user = "willow")
    @JwtSecurity(claims = {@Claim(key = "given_name", value = "Willow"), @Claim(key = "family_name", value = "Johnson")})
    void testReject() {
        assumeThat(!integrationTest); // no matching document in ELN
        assumeThat(documentID).isNotNull();
        DocumentDTO document = signatureClient.rejectDocument(documentID);
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(document.getLastModifiedDate()).isNotEqualTo(document.getCreatedDate());
        assertThat(document.getSignatures()).last().satisfies(block -> {
            assertThat(block.getStatus()).isEqualTo(SignatureStatus.REJECTED);
            assertThat(block.getActionDate()).isNotNull();
            assertThat(block.getUser()).isEqualTo(willowUserRef);
        });
    }

    @Test
    @Order(500)
    void testGetDocuments() {
        assumeThat(!integrationTest); // no matching document in ELN
        assumeThat(documentID).isNotNull();
        List<DocumentDTO> documents = signatureClient.getDocuments();
        assertThat(documents).filteredOn(d -> d.getId().equals(documentID)).hasSize(1).first().satisfies(document -> {
            assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
            assertThat(document.getSignatures()).hasSize(2);
        });
    }

    @Test
    @Order(600)
    void testDownloadDocument() throws Exception {
        assumeThat(!integrationTest); // no matching document in ELN
        assumeThat(documentID).isNotNull();
        Response content = signatureClient.downloadDocument(documentID);
        try (FileOutputStream fos = new FileOutputStream("downloaded.pdf")) {
            fos.write(content.readEntity(byte[].class));
        }
    }
}
