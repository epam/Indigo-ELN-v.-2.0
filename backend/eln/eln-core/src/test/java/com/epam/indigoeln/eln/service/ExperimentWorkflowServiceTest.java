package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.eln.model.NotebookDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectRequest;
import com.epam.indigoeln.eln.model.RevisionSummaryDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateRef;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;
import static com.epam.indigoeln.eln.model.ExperimentStatus.ARCHIVED;
import static com.epam.indigoeln.eln.model.ExperimentStatus.CANCELLED;
import static com.epam.indigoeln.eln.model.ExperimentStatus.COMPLETED;
import static com.epam.indigoeln.eln.model.ExperimentStatus.REJECTED;
import static com.epam.indigoeln.eln.model.ExperimentStatus.REOPEN;
import static com.epam.indigoeln.eln.model.ExperimentStatus.SIGNING;
import static com.epam.indigoeln.eln.model.ExperimentStatus.SUBMITTED;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@ConnectWireMock
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentWorkflowServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    ExperimentDetailsDTO experiment;
    UUID noSignersTemplateID;
    UUID oneSignerTemplateID;
    UUID twoSignersTemplateID;
    UUID documentID;

    WireMock wireMock;

    @TempDir
    File tempDir;
    File mockFile;

    @BeforeAll
    @SneakyThrows
    void setUpAll() {
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

        project = projectClient.createProject(new ProjectRequest("ExperimentWorkflowServiceTest" + UUID.randomUUID()));
        notebook = createNotebook(project.getId());
    }

    @BeforeEach
    void setUp() {
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        documentID = UUID.randomUUID();
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/api/signature/documents/upload")).willReturn(WireMock.aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody("""
                  {"id": "%s", "status": "SUBMITTED", "author": {"username": "john", "displayName": "John Doe"}}
                """.formatted(documentID))
        ));
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
        assertThatClientCall(() -> experimentClient.reopenExperiment(experiment.getId()))
                .isBadRequest("Experiment is OPEN, must be CANCELLED or ARCHIVED");
    }

    @Test
    void testCancel() {
        experiment = experimentClient.cancelExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(CANCELLED);
    }

    @Test
    void testReopenCancelled() {
        experiment = experimentClient.cancelExperiment(experiment.getId());
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenArchived() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenCompleted() {
        experiment = experimentClient.completeExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(COMPLETED);
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenSubmitted() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
    }

    @Test
    void testReopenRejected() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
        rejectDocument(JOHN_USERNAME);
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
    }

    @Test
    void testComplete() {
        experiment = experimentClient.completeExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    void testCompleteReopened() {
        experiment = experimentClient.cancelExperiment(experiment.getId());
        experiment = experimentClient.reopenExperiment(experiment.getId());
        experiment = experimentClient.completeExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    void testSubmitNoSigners() {
        experiment = experimentClient.completeExperiment(experiment.getId());
        experiment = experimentClient.submitExperiment(experiment.getId(), noSignersTemplateID);
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
        assertThat(experiment.getSignatureNumber()).isNotNull();
    }

    @Test
    void testCompleteAndSubmitNoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);
        simulateSignatureUpdate("no signing required", DocumentStatus.SIGNED);
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
        assertThat(experiment.getSignatureNumber()).isNotNull();
    }

    @Test
    void testSubmitOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testApproveOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNED);
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        rejectDocument(BART_USERNAME);
        assertThat(experiment.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testApproveTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        assertThat(experiment.getStatus()).isEqualTo(SIGNING);

        approveDocument(JOHN_USERNAME, DocumentStatus.SIGNED);
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        rejectDocument(JOHN_USERNAME);
        assertThat(experiment.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testResubmitRejected() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        rejectDocument(JOHN_USERNAME);
        experiment = experimentClient.submitExperiment(experiment.getId(), twoSignersTemplateID);
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testVersions() {
        Reaction reaction = experiment.getModel().getReactions().getFirst();
        experimentClient.mutateExperimentModel4(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.mutateExperimentModel4(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);
        experimentClient.reopenExperiment(experiment.getId());
        experimentClient.mutateExperimentModel4(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);

        List<RevisionSummaryDTO> revisions = experimentClient.getExperimentRevisions(experiment.getId(), false);
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

        String diff = experimentClient.getRevisionDiff(experiment.getId(), 7);
        assertThat(diff).contains("REOPEN");
    }

    private void approveDocument(String username, DocumentStatus simulatedStatus) {
        simulateSignatureUpdate(
                "signed by " + username,
                simulatedStatus
        );
    }

    private void rejectDocument(String username) {
        simulateSignatureUpdate("rejected by " + username, DocumentStatus.REJECTED);
    }

    private void simulateSignatureUpdate(String message, DocumentStatus updatedStatus) {
        elnInternalClient.internalSignatureUpdatedClient(documentID, "SIMULATED " + message, updatedStatus, mockFile);
        experiment = experimentClient.getExperiment(experiment.getId());
    }
}
