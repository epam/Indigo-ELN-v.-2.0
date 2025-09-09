package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


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
        experiment = experimentClient.rejectExperiment(experiment.getId());
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
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, null));
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testApproveOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        withUser(BART_USERNAME, () -> {
            experiment = experimentClient.approveExperiment(experiment.getId());
        });
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED));
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectOneSigner() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), oneSignerTemplate.getId());
        withUser(BART_USERNAME, () -> {
            experiment = experimentClient.rejectExperiment(experiment.getId());
        });
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.REJECTED));
        assertThat(experiment.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testApproveTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        withUser(BART_USERNAME, () -> {
            experiment = experimentClient.approveExperiment(experiment.getId());
        });
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED)
                , tuple(getJohnUserRef(), SignatureReason.AUTHOR, null));
        assertThat(experiment.getStatus()).isEqualTo(SIGNING);

        experiment = experimentClient.approveExperiment(experiment.getId());
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED)
                , tuple(getJohnUserRef(), SignatureReason.AUTHOR, SignatureStatus.APPROVED));
        assertThat(experiment.getStatus()).isEqualTo(ARCHIVED);
    }

    @Test
    void testRejectTwoSigners() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        withUser(BART_USERNAME, () -> {
            experiment = experimentClient.approveExperiment(experiment.getId());
        });

        experiment = experimentClient.rejectExperiment(experiment.getId());
        assertSignatures(experiment.getSignatures()
                , tuple(getBartUserRef(), SignatureReason.WITNESS, SignatureStatus.APPROVED)
                , tuple(getJohnUserRef(), SignatureReason.AUTHOR, SignatureStatus.REJECTED));
        assertThat(experiment.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void testResubmitRejected() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
        experiment = experimentClient.rejectExperiment(experiment.getId());
        experiment = experimentClient.resubmitExperiment(experiment.getId());
        assertThat(experiment.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void testGetExperimentsForSignature() {
        experiment = experimentClient.completeAndSubmitExperiment(experiment.getId(), twoSignersTemplate.getId());
    }

    private void assertSignatures(List<ExperimentSignature> signatures, Tuple... expected) {
        assertThat(signatures).map(ExperimentSignature::getUser, ExperimentSignature::getReason, ExperimentSignature::getStatus).containsExactly(expected);
    }
}
