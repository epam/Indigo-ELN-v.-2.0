package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.signature.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
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

import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentWorkflowServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    ExperimentDetailsDTO experiment;
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
            when(reportsClient.generateExperimentReport(any()))
                    .thenAnswer(inv -> Response.ok("contentcontentcontent".getBytes()).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"").build());
            noSignersTemplateID = UUID.randomUUID();
            oneSignerTemplateID = UUID.randomUUID();
            twoSignersTemplateID = UUID.randomUUID();
            when(signatureClient.getTemplates())
                    .thenReturn(List.of(
                            createMockTemplateDTO(noSignersTemplateID, "ExperimentWorkflowServiceTest-noSigners"),
                            createMockTemplateDTO(oneSignerTemplateID, "ExperimentWorkflowServiceTest-oneSigner"),
                            createMockTemplateDTO(twoSignersTemplateID, "ExperimentWorkflowServiceTest-twoSigners")
                    ));
            mockFile = new File(tempDir, "updated.txt");
            Files.write(mockFile.toPath(), "updatedcontent".getBytes());
        } else {
            noSignersTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-noSigners", List.of(
                    )))
                    .getId();
            oneSignerTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-oneSigner", List.of(
                        new SignatureTemplateBlock(getBartUserRef(), SignatureReason.WITNESS)
                    )))
                    .getId();
            twoSignersTemplateID = signatureClient.createTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-twoSigners", List.of(
                        new SignatureTemplateBlock(getBartUserRef(), SignatureReason.WITNESS),
                        new SignatureTemplateBlock(null, SignatureReason.AUTHOR)
                    )))
                    .getId();
        }

        project = projectClient.createProject(new ProjectRequest("ExperimentWorkflowServiceTest" + UUID.randomUUID()));
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
    }

    @BeforeEach
    void setUp() {
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        if (!integrationTest) {
            documentID = UUID.randomUUID();
            DocumentDTO document = new DocumentDTO();
            document.setId(documentID);
            document.setStatus(DocumentStatus.SUBMITTED);
            when(signatureClient.uploadDocumentClient(any(), any(), any()))
                    .thenReturn(document);
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
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.WAITING)
        );
    }

    @Test
    void testApproveOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNED);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED)
        );
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplateID);
        rejectDocument(BART_USERNAME);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.REJECTED)
        );
        assertThat(experiment.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testApproveTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        verifySignature(
            tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
            tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.WAITING)
        );
        assertThat(experiment.getStatus()).isEqualTo(SIGNING);

        approveDocument(JOHN_USERNAME, DocumentStatus.SIGNED);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
                tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.APPROVED)
        );
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplateID);
        approveDocument(BART_USERNAME, DocumentStatus.SIGNING);
        rejectDocument(JOHN_USERNAME);
        verifySignature(
                tuple(BART_USERNAME, SignatureReason.WITNESS, SignatureStatus.APPROVED),
                tuple(JOHN_USERNAME, SignatureReason.AUTHOR, SignatureStatus.REJECTED)
        );
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
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);
        experimentClient.reopenExperiment(experiment.getId());
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplateID);

        List<RevisionDetailsDTO> revisions = experimentClient.getExperimentRevisions(experiment.getId(), null, null);
        assertThat(revisions).<Class<?>>map(r -> r.getMutation().getClass()).containsExactly(
                ExperimentMutation.CreateExperiment.class,
                ReactionMutation.AddEmptyInput.class,
                ReactionMutation.AddEmptyInput.class,
                ExperimentMutation.CompleteExperiment.class,
                ExperimentMutation.MakeVersion.class,
                ExperimentMutation.SubmitExperiment.class,
                ExperimentMutation.ReopenExperiment.class,
                ReactionMutation.AddEmptyInput.class,
                ExperimentMutation.CompleteExperiment.class,
                ExperimentMutation.MakeVersion.class,
                ExperimentMutation.SubmitExperiment.class
        );

        List<ExperimentRevisionSummaryDTO> revisionsSummary = experimentClient.getExperimentRevisionsSummary(experiment.getId()).reversed();
        assertThat(revisionsSummary).map(ExperimentRevisionSummaryDTO::getSummary).containsExactly(
                "Experiment created",
                "Edited experiment",
                "Experiment completed",
                "Version 1",
                "Experiment submitted for signature",
                "Experiment reopened",
                "Edited experiment",
                "Experiment completed",
                "Version 2",
                "Experiment submitted for signature"
        );
        ExperimentRevisionSummaryDTO firstEditSession = revisionsSummary.get(1);

        List<RevisionDetailsDTO> editRevisions = experimentClient.getExperimentRevisions(experiment.getId(), firstEditSession.getEditSessionID(), null);
        assertThat(editRevisions).map(RevisionDetailsDTO::getRevision).containsExactly(
                revisions.get(1).getRevision(), revisions.get(2).getRevision()
        );

        JsonNode versionDiff = experimentClient.compareVersions(experiment.getId(), 1, 2);
        assertThat(versionDiff).isNotNull();

        experimentClient.compareVersions(experiment.getId(), 1, null);
        experimentClient.compareVersionsHTML(experiment.getId(), 1, null);
    }

    private void approveDocument(String username, DocumentStatus simulatedStatus) {
        if (integrationTest) {
            UUID documentId = UUID.fromString(checkNotNull(experiment.getSignatureNumber()));
            withUser(username, () -> signatureClient.signDocument(documentId));
            this.experiment = experimentClient.getExperiment(experiment.getId());
        } else {
            simulateSignatureUpdate(
                    "signed by " + username,
                    simulatedStatus
            );
        }
    }

    private void rejectDocument(String username) {
        if (integrationTest) {
            UUID documentId = UUID.fromString(checkNotNull(experiment.getSignatureNumber()));
            withUser(username, () -> signatureClient.rejectDocument(documentId));
            this.experiment = experimentClient.getExperiment(experiment.getId());
        } else {
            simulateSignatureUpdate("rejected by " + username, DocumentStatus.REJECTED);
        }
    }

    private SignatureTemplateDTO createMockTemplateDTO(UUID id, String name) {
        SignatureTemplateDTO dto = new SignatureTemplateDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    private void verifySignature(Tuple... tuples) {
        if (integrationTest) {
            DocumentDTO document = signatureClient.getDocument(UUID.fromString(checkNotNull(experiment.getSignatureNumber())));
            assertThat(document.getSignatures())
                    .map(x -> x.getUser().getUsername(), DocumentSignatureDTO::getReason, DocumentSignatureDTO::getStatus)
                    .containsExactly(tuples);
        }
    }

    private void simulateSignatureUpdate(String message, DocumentStatus updatedStatus) {
        if (!integrationTest) {
            elnInternalClient.internalSignatureUpdatedClient(documentID, "SIMULATED " + message, updatedStatus, mockFile);
            experiment = experimentClient.getExperiment(experiment.getId());
        }
    }
}
