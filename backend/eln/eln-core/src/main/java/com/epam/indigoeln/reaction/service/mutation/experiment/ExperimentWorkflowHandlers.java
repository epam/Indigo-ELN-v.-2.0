package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static com.epam.indigoeln.common.util.ModelUtil.useTempFile;
import static com.epam.indigoeln.eln.model.ApplicationPermission.SUBMIT_EXPERIMENTS;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class ExperimentWorkflowMutationHandlerBase<T extends ExperimentMutation> extends AbstractExperimentMutationHandler<T> {

    public void updateStatusFromSignature(ExperimentEntity experiment, DocumentStatus documentStatus) {
        switch (documentStatus) {
            case SIGNING -> {
                ensureStatus(experiment, SUBMITTED, SIGNING);
                experiment.setStatus(SIGNING);
            }
            case SIGNED -> {
                ensureStatus(experiment, SUBMITTED, SIGNING);
                experiment.setStatus(SIGNED);
                experiment.setStatus(ARCHIVED);
            }
            case REJECTED -> {
                ensureStatus(experiment, SUBMITTED, SIGNING, REJECTED);
                experiment.setStatus(REJECTED);
            }
        }
    }

}

@Dependent
@MutationHandlerFor(ExperimentMutation.CancelExperiment.class)
class CancelExperimentHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.CancelExperiment> {

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        aclService.ensureAccess(entity, SUBMIT_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        ensureStatus(entity, OPEN, REOPEN);
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CancelExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(CANCELLED);
        return "Experiment cancelled";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ReopenExperiment.class)
class ReopenExperimentHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.ReopenExperiment> {

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        aclService.ensureAccess(entity, SUBMIT_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        ensureStatus(entity, CANCELLED, ARCHIVED, COMPLETED, SUBMITTED, REJECTED);
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.ReopenExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(REOPEN);
        return "Experiment reopened";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CompleteExperiment.class)
class CompleteExperimentHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.CompleteExperiment> {

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        aclService.ensureAccess(entity, SUBMIT_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        ensureStatus(entity, OPEN, REOPEN);
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CompleteExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(COMPLETED);
        return "Experiment completed";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SubmitExperiment.class)
class SubmitExperimentHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.SubmitExperiment> {

    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    @RestClient
    SignatureClient signatureClient;

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        aclService.ensureAccess(entity, SUBMIT_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        ensureStatus(entity, COMPLETED, REJECTED);
    }

    @Override
    @SneakyThrows
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.SubmitExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(SUBMITTED);
        ExperimentService.ExperimentReportContent report = experimentService.printReport(experiment);
        AttachmentEntity attachment = attachmentService.createExperimentAttachment(experiment, report.filename(), report.content(), false);
        String documentName = experiment.getName() + (experiment.getVersion() != null ? ", version " + experiment.getVersion() : "");
        DocumentDTO document = useTempFile(attachment.getName(), attachment.getContent(), file -> {
            return signatureClient.uploadDocumentClient(documentName, mutation.signatureTemplateID(), file);
        });
        experiment.setSignatureNumber(document.getId().toString());
        experiment.setSignatureAttachment(attachment);
        updateStatusFromSignature(experiment, document.getStatus());
        return "Experiment submitted for signature";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SignatureUpdated.class)
class SignatureUpdatedHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.SignatureUpdated> {

    @Inject
    AttachmentService attachmentService;
    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        // nothing
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.SignatureUpdated mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        updateStatusFromSignature(experiment, mutation.documentStatus());
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddExperimentAttachment(experiment, attachment);
        return "Signatures update: " + mutation.message();
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.MakeVersion.class)
class MakeVersionHandler extends ExperimentWorkflowMutationHandlerBase<ExperimentMutation.MakeVersion> {

    @Inject
    ExperimentRepository experimentRepository;

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        // nothing
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.MakeVersion mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        int lastUsedVersion = MoreObjects.firstNonNull(experimentRepository.getLastUsedVersion(experiment), 0);
        context.setCreatedVersion(lastUsedVersion + 1);
        experiment.setVersion(context.getCreatedVersion());
        return "Version " + context.getCreatedVersion();
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.MakeVersion mutation, String summary, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, summary, revisionNo, patch, context, snapshotAfter);
        revision.setVersion(checkNotNull(context.getCreatedVersion()));
        revision.setSnapshot(snapshotAfter);
        return revision;
    }
}
