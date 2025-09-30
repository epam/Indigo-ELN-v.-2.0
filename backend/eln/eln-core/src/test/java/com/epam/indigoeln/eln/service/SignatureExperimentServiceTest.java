package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.eln.test.SignaturesAssert.assertThatSignatures;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class SignatureExperimentServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    ExperimentDetailsDTO experiment;
    SignatureTemplateDetailsDTO twoSignersTemplate;

    @BeforeAll
    void setUpAll() {
        project = projectClient.createProject(new ProjectRequest("ExperimentWorkflowServiceTest" + UUID.randomUUID()));
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        twoSignersTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-twoSigners"
                , List.of(new SignatureBlock(getBartUserRef(), SignatureReason.WITNESS), new SignatureBlock(null, SignatureReason.AUTHOR))));
        withUser(JOHN_USERNAME, () -> {
            experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        });
    }

    @Test
    @Order(1)
    void testGetExperimentsForSignature() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        Page<ExperimentForSignatureDTO> page = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
        assertThat(page.getItems()).hasSize(1);
        ExperimentForSignatureDTO forSignature = page.getItems().getFirst();
        assertThat(forSignature.getId()).isEqualTo(experiment.getId());
        assertThat(forSignature.getName()).isEqualTo(experiment.getName());
        assertThatSignatures(forSignature.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, null,
                getJohnUserRef(), SignatureReason.AUTHOR, null
        );
        withUser(BART_USERNAME, () -> {
            Page<ExperimentForSignatureDTO> pageForBart = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
            assertThat(pageForBart.getItems()).hasSize(1);
            ExperimentForSignatureDTO forBartSignature = pageForBart.getItems().getFirst();
            assertThat(forBartSignature.getId()).isEqualTo(experiment.getId());
            assertThat(forBartSignature.getName()).isEqualTo(experiment.getName());
            assertThatSignatures(forBartSignature.getSignatures()).containsOnly(
                    getBartUserRef(), SignatureReason.WITNESS, null,
                    getJohnUserRef(), SignatureReason.AUTHOR, null
            );
        });
        withUser(LISA_USERNAME, () -> {
            assertThat(signatureClient.getExperimentsForSignature(Paging.DEFAULT).getItems()).isEmpty();
        });
    }

    @Test
    @Order(2)
    void testDownloadReportForSignature() {
        try (Response response = signatureClient.downloadReportForSignature(experiment.getId())) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }
    }

    @Test
    @Order(3)
    void testOneSigned() {
        experiment = experimentClient.approveExperiment(experiment.getId());
        Page<ExperimentForSignatureDTO> page = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
        assertThat(page.getItems()).isEmpty();
        withUser(BART_USERNAME, () -> {
            Page<ExperimentForSignatureDTO> pageForBart = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
            ExperimentForSignatureDTO forBartSignature = pageForBart.getItems().getFirst();
            assertThat(forBartSignature.getId()).isEqualTo(experiment.getId());
            assertThat(forBartSignature.getName()).isEqualTo(experiment.getName());
            assertThatSignatures(forBartSignature.getSignatures()).containsOnly(
                    getBartUserRef(), SignatureReason.WITNESS, null,
                    getJohnUserRef(), SignatureReason.AUTHOR, SignatureStatus.APPROVED
            );
        });
    }

    @Test
    @Order(4)
    void testBothSigned() {
        withUser(BART_USERNAME, () -> {
            experiment = experimentClient.approveExperiment(experiment.getId());
            Page<ExperimentForSignatureDTO> pageForBart = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
            assertThat(pageForBart.getItems()).isEmpty();
        });
        Page<ExperimentForSignatureDTO> page = signatureClient.getExperimentsForSignature(Paging.DEFAULT);
        assertThat(page.getItems()).isEmpty();
    }
}
