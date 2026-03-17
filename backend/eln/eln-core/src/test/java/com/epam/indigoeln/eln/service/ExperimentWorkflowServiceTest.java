package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.epam.indigoeln.eln.test.SignaturesAssert.assertThatSignatures;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentWorkflowServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    ExperimentDetailsDTO experiment;
    SignatureTemplateDetailsDTO noSignersTemplate;
    SignatureTemplateDetailsDTO oneSignerTemplate;
    SignatureTemplateDetailsDTO twoSignersTemplate;

    @BeforeAll
    void setUpAll() {
        project = projectClient.createProject(new ProjectRequest("ExperimentWorkflowServiceTest" + UUID.randomUUID()));
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        noSignersTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-noSigners"
                , List.of()));
        oneSignerTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-oneSigner"
                , List.of(new SignatureBlock(getBartUserRef(), SignatureReason.WITNESS))));
        twoSignersTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("ExperimentWorkflowServiceTest-twoSigners"
                , List.of(new SignatureBlock(getBartUserRef(), SignatureReason.WITNESS), new SignatureBlock(null, SignatureReason.AUTHOR))));
        if (mockReportsClient != null) {
            Mockito.when(mockReportsClient.generateExperimentReport(any()))
                    .thenAnswer(inv -> Response.ok(new byte[0]).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"").build());
        }
    }

    @BeforeEach
    void setUp() {
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
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
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplate.getId());
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
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
        assertThat(experiment.getSignatures()).isEmpty();
    }

    @Test
    void testReopenRejected() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
        experimentClient.rejectExperiment(experiment.getId());
        experiment = experimentClient.reopenExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(REOPEN);
        assertThat(experiment.getSignatures()).isEmpty();
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
        experiment = experimentClient.submitExperiment(experiment.getId(), noSignersTemplate.getId());
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testCompleteAndSubmitNoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplate.getId());
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testSubmitOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        assertThatSignatures(experiment.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, null
        );
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testApproveOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        ExperimentForSignatureDTO experimentForSignature = withUser(BART_USERNAME
                , () -> experimentClient.approveExperiment(experiment.getId()));
        assertThatSignatures(experimentForSignature.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED
        );
        assertThat(experimentForSignature.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        ExperimentForSignatureDTO experimentForSignature = withUser(BART_USERNAME
                , () -> experimentClient.rejectExperiment(experiment.getId()));
        assertThatSignatures(experimentForSignature.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.REJECTED
        );
        assertThat(experimentForSignature.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testApproveTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        ExperimentForSignatureDTO experimentForSignature = withUser(BART_USERNAME
                , () -> experimentClient.approveExperiment(experiment.getId()));
        assertThatSignatures(experimentForSignature.getSignatures()).containsOnly(
            getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED,
            getJohnUserRef(), SignatureReason.AUTHOR, null
        );
        assertThat(experimentForSignature.getStatus()).isEqualTo(SIGNING);

        experimentForSignature = experimentClient.approveExperiment(experiment.getId());
        assertThatSignatures(experimentForSignature.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED,
                getJohnUserRef(), SignatureReason.AUTHOR, SignatureStatus.APPROVED
        );
        assertThat(experimentForSignature.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        ExperimentForSignatureDTO experimentForSignature = withUser(BART_USERNAME
                , () -> experimentClient.approveExperiment(experiment.getId()));

        experimentForSignature = experimentClient.rejectExperiment(experimentForSignature.getId());
        assertThatSignatures(experimentForSignature.getSignatures()).containsOnly(
                getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED,
                getJohnUserRef(), SignatureReason.AUTHOR, SignatureStatus.REJECTED
        );
        assertThat(experimentForSignature.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testResubmitRejected() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        ExperimentForSignatureDTO experimentForSignature = experimentClient.rejectExperiment(experiment.getId());
        experiment = experimentClient.resubmitExperiment(experimentForSignature.getId());
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testVersions() {
        Reaction reaction = experiment.getModel().getReactions().getFirst();
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplate.getId());
        experimentClient.reopenExperiment(experiment.getId());
        experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        experimentClient.completeAndSubmitExperiment(experiment.getId(), noSignersTemplate.getId());

        List<RevisionDetailsDTO<ExperimentPatch>> revisions = experimentClient.getExperimentRevisions(experiment.getId(), null, null);
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

        List<RevisionDetailsDTO<ExperimentPatch>> editRevisions = experimentClient.getExperimentRevisions(experiment.getId(), firstEditSession.getEditSessionID(), null);
        assertThat(editRevisions).map(RevisionDetailsDTO::getRevision).containsExactly(
                revisions.get(1).getRevision(), revisions.get(2).getRevision()
        );

        ExperimentPatch versionDiff = experimentClient.compareVersions(experiment.getId(), 1, 2);
        //noinspection DataFlowIssue
        assertThat(versionDiff.getModel().updatedValue().getReactions().updatedValue().getItems().getFirst().value().updatedValue().getInputs().updatedValue().getItems()).singleElement().satisfies(input -> {
            assertThat(input.oldIndex()).isNull();
            assertThat(input.newIndex()).isEqualTo(2);
        });

        experimentClient.compareVersions(experiment.getId(), 1, null);
        experimentClient.compareVersionsHTML(experiment.getId(), 1, null);
    }
}
