package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static com.epam.indigoeln.common.util.ModelUtil.useTempFile;
import static com.epam.indigoeln.eln.model.ApplicationPermission.SUBMIT_EXPERIMENTS;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ExperimentMutation.CancelExperiment.class)
class CancelExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CancelExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CancelExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, CANCELLED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return "Experiment cancelled";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ReopenExperiment.class)
class ReopenExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.ReopenExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.ReopenExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, REOPEN, SUBMIT_EXPERIMENTS, CANCELLED, ARCHIVED, COMPLETED, SUBMITTED, REJECTED);
        return "Experiment reopened";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CompleteExperiment.class)
class CompleteExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CompleteExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CompleteExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, COMPLETED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return "Experiment completed";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SubmitExperiment.class)
class SubmitExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.SubmitExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;
    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    @RestClient
    SignatureClient signatureClient;

    @Override
    @SneakyThrows
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.SubmitExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, COMPLETED, REJECTED);
        ExperimentService.ExperimentReportContent report = experimentService.printReport(experiment);
        AttachmentEntity attachment = attachmentService.createExperimentAttachment(experiment, report.filename(), report.content(), false);
        String documentName = experiment.getName() + (experiment.getVersion() != null ? ", version " + experiment.getVersion() : "");
        DocumentDTO document = useTempFile(attachment.getName(), attachment.getContent(), file -> {
            return signatureClient.uploadDocumentClient(documentName, mutation.signatureTemplateID(), file);
        });
        experiment.setSignatureNumber(document.getId().toString());
        experiment.setSignatureAttachment(attachment);
        helper.updateStatusFromSignature(experiment, document.getStatus());
        return "Experiment submitted for signature";
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SignatureUpdated.class)
class SignatureUpdatedHandler extends ExperimentMutationHandlerBase<ExperimentMutation.SignatureUpdated> {

    @Inject
    ExperimentWorkflowHelper helper;
    @Inject
    AttachmentService attachmentService;
    @Inject
    AttachmentRepository attachmentRepository;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.SignatureUpdated mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.updateStatusFromSignature(experiment, mutation.documentStatus());
        AttachmentEntity attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddExperimentAttachment(experiment, attachment);
        return "Signatures update: " + mutation.message();
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.MakeVersion.class)
class MakeVersionHandler extends ExperimentMutationHandlerBase<ExperimentMutation.MakeVersion> {

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

@ApplicationScoped
class ExperimentWorkflowHelper {

    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    void transition(ExperimentEntity experiment, ExperimentStatus targetStatus, @Nullable ApplicationPermission requiredAccess, ExperimentStatus... allowedStatuses) {
        if (requiredAccess != null) {
            aclService.ensureAccess(experiment, requiredAccess);
        }
        ensureStatus(experiment, allowedStatuses);
        experiment.setStatus(targetStatus);
    }

    void ensureStatus(ExperimentEntity experiment, ExperimentStatus... allowedStatuses) {
        if (!Arrays.asList(allowedStatuses).contains(experiment.getStatus())) {
            InvalidRequestException.fail("Experiment is " + experiment.getStatus() + ", must be " + StreamEx.of(allowedStatuses).joining(" or "));
        }
    }

    void updateStatusFromSignature(ExperimentEntity experiment, DocumentStatus documentStatus) {
        switch (documentStatus) {
            case SIGNING -> {
                if (experiment.getStatus() != SIGNING) {
                    transition(experiment, SIGNING, null, SUBMITTED);
                }
            }
            case SIGNED -> {
                transition(experiment, SIGNED, null, SUBMITTED, SIGNING);
                transition(experiment, ARCHIVED, null, SIGNED);
            }
            case REJECTED -> {
                transition(experiment, REJECTED, null, SUBMITTED, SIGNING);
            }
        }
    }
}
