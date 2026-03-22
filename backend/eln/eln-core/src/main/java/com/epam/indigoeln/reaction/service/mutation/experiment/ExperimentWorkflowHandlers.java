package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.SignatureStatus;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.eln.model.ApplicationPermission.SUBMIT_EXPERIMENTS;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ExperimentMutation.CancelExperiment.class)
class CancelExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CancelExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.CancelExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, CANCELLED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return new MutationResult("Experiment cancelled");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ReopenExperiment.class)
class ReopenExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.ReopenExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.ReopenExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, REOPEN, SUBMIT_EXPERIMENTS, CANCELLED, ARCHIVED, COMPLETED, SUBMITTED, REJECTED);
        experiment.getSignatures().clear();
        return new MutationResult("Experiment reopened");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CompleteExperiment.class)
class CompleteExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.CompleteExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.CompleteExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, COMPLETED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return new MutationResult("Experiment completed");
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
    SignatureTemplateRepository signatureTemplateRepository;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.SubmitExperiment mutation, ExperimentMutationContext context) {
        context.setAffectsAttachments(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.SubmitExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        SignatureTemplateEntity signatureTemplate = signatureTemplateRepository.get(mutation.signatureTemplateID());
        helper.transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, COMPLETED);
        ExperimentService.ExperimentReportContent report = experimentService.printReport(experiment);
        AttachmentEntity attachment = attachmentService.createExperimentAttachment(experiment, report.filename(), report.content(), false);
        experiment.setReportForSignature(attachment);
        experiment.getSignatures().clear();
        experiment.getSignatures().addAll(signatureTemplate.getBlocks().stream()
                .map(block -> {
                    UserEntity user = switch (block.getReason()) {
                        case WITNESS -> checkNotNull(block.getUser());
                        case AUTHOR -> experiment.getCreatedBy();
                    };
                    return new ExperimentSignatureEntity(experiment, user, block.getReason(), null, null);
                })
                .toList()
        );
        helper.doCheckSignatures(experiment);
        return new MutationResult("Experiment submitted for signature");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ApproveExperiment.class)
class ApproveExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.ApproveExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.ApproveExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.doApproveOrReject(SignatureStatus.APPROVED, experiment);
        return new MutationResult("Experiment approved");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.RejectExperiment.class)
class RejectExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.RejectExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.RejectExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.doApproveOrReject(SignatureStatus.REJECTED, experiment);
        return new MutationResult("Experiment rejected");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ResubmitExperiment.class)
class ResubmitExperimentHandler extends ExperimentMutationHandlerBase<ExperimentMutation.ResubmitExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.ResubmitExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        helper.transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, REJECTED);
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            signature.setStatus(null);
        }
        return new MutationResult("Experiment resubmitted for signature");
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.MakeVersion.class)
class MakeVersionHandler extends ExperimentMutationHandlerBase<ExperimentMutation.MakeVersion> {

    @Inject
    ExperimentRepository experimentRepository;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.MakeVersion mutation, ExperimentMutationContext context) {
        // make sure snapshot contains all fields
        context.setAffectsModel(true);
        context.setAffectsAttachments(true);
        context.setAffectsACL(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentMutation.MakeVersion mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        int lastUsedVersion = MoreObjects.firstNonNull(experimentRepository.getLastUsedVersion(experiment), 0);
        context.setCreatedVersion(lastUsedVersion + 1);
        return new MutationResult("Version " + context.getCreatedVersion());
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.MakeVersion mutation, MutationResult result, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, result, revisionNo, patch, context, snapshotAfter);
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

    void doCheckSignatures(ExperimentEntity experiment) {
        boolean hasPending = false, hasApproved = false, hasRejected = false;
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            switch (signature.getStatus()) {
                case null -> hasPending = true;
                case APPROVED -> hasApproved = true;
                case REJECTED -> hasRejected = true;
            }
        }
        if (hasRejected) {
            transition(experiment, REJECTED, null, SUBMITTED, SIGNING);
            return;
        }
        if (!hasPending) {
            transition(experiment, SIGNED, null, SUBMITTED, SIGNING);
            transition(experiment, ARCHIVED, null, SIGNED);
            return;
        }
        if (hasApproved && experiment.getStatus() == SUBMITTED) {
            transition(experiment, SIGNING, null, SUBMITTED);
        }
    }

    void doApproveOrReject(SignatureStatus status, ExperimentEntity experiment) {
        boolean found = false;
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            if (signature.getUser().equals(userService.getCurrentUserEntity())) {
                validate(signature.getStatus() == null, "Experiment was already approved or rejected by " + userService.getCurrentUser());
                signature.setStatus(status);
                found = true;
            }
        }
        validate(found, userService.getCurrentUser().getUsername() + " is not listed as a signer of experiment " + experiment.getName());
        doCheckSignatures(experiment);
    }
}
