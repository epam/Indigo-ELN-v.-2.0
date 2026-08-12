package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.util.ExperimentObject;
import com.epam.indigoeln.signature.model.*;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@QuarkusTest
@ConnectWireMock
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentWorkflowServiceTest extends ELNBaseTest {

    WireMock wireMock;

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    ExperimentObject experiment;
    UUID noSignersTemplateID;
    UUID oneSignerTemplateID;
    UUID twoSignersTemplateID;
    UUID documentID;

    @TempDir
    File tempDir;
    File mockFile;

    @BeforeAll
    @SneakyThrows
    void setUpAll() {
        if (!integrationTest) {
            wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/internalapi/reports/experiment")).willReturn(WireMock.aResponse()
                    .withHeader(HttpHeaders.CONTENT_DISPOSITION,  generateContentDisposition(true, "report.pdf"))
                    .withBody("\"content content content\"")
            ));
            noSignersTemplateID = UUID.randomUUID();
            oneSignerTemplateID = UUID.randomUUID();
            twoSignersTemplateID = UUID.randomUUID();
            wireMock.register(WireMock.get(WireMock.urlPathEqualTo("/api/signature/templates")).willReturn(WireMock.aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody("""
                            [
                                {"id": "%s", "name": "ExperimentWorkflowServiceTest-noSigners"},
                                {"id": "%s", "name": "ExperimentWorkflowServiceTest-oneSigner"},
                                {"id": "%s", "name": "ExperimentWorkflowServiceTest-twoSigners"}
                            ]
                            """.formatted(noSignersTemplateID, oneSignerTemplateID, twoSignersTemplateID)
                    )
            ));
            mockFile = new File(tempDir, "updated.txt");
            Files.write(mockFile.toPath(), "updatedcontent".getBytes());
        } else {
            noSignersTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-noSigners", List.of(
                    )))
                    .getId();
            oneSignerTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-oneSigner", List.of(
                        new SignatureTemplateBlock(BART_USER_REF, SignatureReason.WITNESS)
                    )))
                    .getId();
            twoSignersTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-twoSigners", List.of(
                        new SignatureTemplateBlock(BART_USER_REF, SignatureReason.WITNESS),
                        new SignatureTemplateBlock(null, SignatureReason.AUTHOR)
                    )))
                    .getId();
        }

        project = projectClient.createProject(new ProjectRequest("ExperimentWorkflowServiceTest" + UUID.randomUUID()));
        notebook = createNotebook(project.getId());
    }

    @BeforeEach
    void setUp() {
        experiment = createExperiment(notebook, new ExperimentRequest(emptyTemplateID));
        if (!integrationTest) {
            documentID = UUID.randomUUID();
            wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/api/signature/documents/upload")).willReturn(WireMock.aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody("""
                      {"id": "%s", "status": "SUBMITTED", "author": {"username": "john", "displayName": "John Doe"}}
                    """.formatted(documentID))
            ));
        }
    }

    @Test
    void testGetSignatureTemplates() {
        assertThat(experimentClient.getSignatureTemplates())
                .containsExactlyInAnyOrder(
                        new SignatureTemplateRef(noSignersTemplateID, "ExperimentWorkflowServiceTest-noSigners"),
                        new SignatureTemplateRef(oneSignerTemplateID, "ExperimentWorkflowServiceTest-oneSigner"),
                        new SignatureTemplateRef(twoSignersTemplateID, "ExperimentWorkflowServiceTest-twoSigners")
                );
    }

    @Test
    void testIncorrectStatus() {
        assertThatClientCall(() -> experimentClient.reopenExperiment(experiment.id()))
                .isBadRequest("Experiment is OPEN, must be CANCELLED or ARCHIVED");
    }

    @Test
    void testCancel() {
        experiment.update(experimentClient.cancelExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(CANCELLED);
    }

    @Test
    void testReopenCancelled() {
        experiment.update(experimentClient.cancelExperiment(experiment.id()));
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenArchived() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), noSignersTemplateID));
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.status()).isEqualTo(ARCHIVED);
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenCompleted() {
        experiment.update(experimentClient.completeExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(COMPLETED);
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenSubmitted() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), oneSignerTemplateID));
        assertThat(experiment.status()).isEqualTo(SUBMITTED);
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenRejected() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), twoSignersTemplateID));
        assertThat(experiment.status()).isEqualTo(SUBMITTED);
        rejectDocument(JOHN_USERNAME);
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(REOPEN);
    }

    @Test
    void testComplete() {
        experiment.update(experimentClient.completeExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(COMPLETED);
    }

    @Test
    void testCompleteReopened() {
        experiment.update(experimentClient.cancelExperiment(experiment.id()));
        experiment.update(experimentClient.reopenExperiment(experiment.id()));
        experiment.update(experimentClient.completeExperiment(experiment.id()));
        assertThat(experiment.status()).isEqualTo(COMPLETED);
    }

    @Test
    void testSubmitNoSigners() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.update(experimentClient.completeExperiment(experiment.id()));
        experiment.update(experimentClient.submitExperiment(experiment.id(), noSignersTemplateID));
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.status()).isEqualTo(ARCHIVED);
        assertThat(experiment.experiment().getSignatureNumber()).isNotNull();
    }

    @Test
    void testCompleteAndSubmitNoSigners() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), noSignersTemplateID));
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.status()).isEqualTo(ARCHIVED);
        assertThat(experiment.experiment().getSignatureNumber()).isNotNull();
    }

    @Test
    void testSubmitOneSigner() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), oneSignerTemplateID));
        assertThat(experiment.status()).isEqualTo(SUBMITTED);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.WAITING)
        );
    }

    @Test
    void testApproveOneSigner() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), oneSignerTemplateID));
        approveDocument(BART_USERNAME, DocumentStatus.SIGNED);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED)
        );
        assertThat(experiment.status()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectOneSigner() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), oneSignerTemplateID));
        rejectDocument(BART_USERNAME);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.REJECTED)
        );
        assertThat(experiment.status()).isEqualTo(REJECTED);
    }

    @Test
    void testApproveTwoSigners() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), twoSignersTemplateID));
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        verifySignature(
            tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
            tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.WAITING)
        );
        assertThat(experiment.status()).isEqualTo(SIGNING);

        approveDocument(JOHN_USERNAME, DocumentStatus.SIGNED);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
                tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.APPROVED)
        );
        assertThat(experiment.status()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectTwoSigners() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), twoSignersTemplateID));
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        rejectDocument(JOHN_USERNAME);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
                tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.REJECTED)
        );
        assertThat(experiment.status()).isEqualTo(REJECTED);
    }

    @Test
    void testResubmitRejected() {
        experiment.update(experimentClient.completeAndSubmitExperiment(experiment.id(), twoSignersTemplateID));
        rejectDocument(JOHN_USERNAME);
        experiment.update(experimentClient.submitExperiment(experiment.id(), twoSignersTemplateID));
        assertThat(experiment.status()).isEqualTo(SUBMITTED);
    }

    @Test
    void testVersions() {
        Reaction reaction = experiment.reaction();
        experimentClient.mutateExperimentModel4(experiment.id(), experiment.revision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.mutateExperimentModel4(experiment.id(), experiment.revision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.id(), noSignersTemplateID);
        experimentClient.reopenExperiment(experiment.id());
        experimentClient.mutateExperimentModel4(experiment.id(), experiment.revision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.id(), noSignersTemplateID);

        List<RevisionSummaryDTO> revisions = experimentClient.getExperimentRevisions(experiment.id(), false);
        assertThat(revisions).map(RevisionSummaryDTO::getSummary).containsExactly(
                "Experiment created",
                "Edited experiment",
                "Experiment completed",
                "Version 1",
                "Experiment submitted for signature",
                "Experiment reopened",
                "Add empty input",
                "Experiment completed",
                "Version 2",
                "Experiment submitted for signature"
        );
        RevisionSummaryDTO firstEditSession = revisions.get(1);

        List<RevisionSummaryDTO> editRevisions = firstEditSession.getDetails();
        assertThat(editRevisions).map(RevisionSummaryDTO::getRevision).containsExactly(2, 3);

        String diff = experimentClient.getRevisionDiff(experiment.id(), 7);
        assertThat(diff).contains("REOPEN");
    }

    private void approveDocument(String username, DocumentStatus simulatedStatus) {
        if (integrationTest) {
            UUID documentId = UUID.fromString(checkNotNull(experiment.experiment().getSignatureNumber()));
            withUser(username, () -> signatureClient.signDocument(documentId));
            experiment.update(experimentClient.getExperiment(experiment.id()));
        } else {
            simulateSignatureUpdate(
                    "signed by " + username,
                    simulatedStatus
            );
        }
    }

    private void rejectDocument(String username) {
        if (integrationTest) {
            UUID documentId = UUID.fromString(checkNotNull(experiment.experiment().getSignatureNumber()));
            withUser(username, () -> signatureClient.rejectDocument(documentId));
            experiment.update(experimentClient.getExperiment(experiment.id()));
        } else {
            simulateSignatureUpdate("rejected by " + username, DocumentStatus.REJECTED);
        }
    }

    private void verifySignature(Tuple... tuples) {
        if (integrationTest) {
            DocumentDTO document = signatureClient.getDocument(UUID.fromString(checkNotNull(experiment.experiment().getSignatureNumber())));
            assertThat(document.getSignatures())
                    .map(x -> x.getUser().getUsername(), DocumentSignatureDTO::getReason, DocumentSignatureDTO::getStatus)
                    .containsExactly(tuples);
        }
    }

    private void simulateSignatureUpdate(String message, DocumentStatus updatedStatus) {
        if (!integrationTest) {
            elnInternalClient.internalSignatureUpdatedClient(documentID, "SIMULATED " + message, updatedStatus, mockFile);
            experiment.update(experimentClient.getExperiment(experiment.id()));
        }
    }
}
