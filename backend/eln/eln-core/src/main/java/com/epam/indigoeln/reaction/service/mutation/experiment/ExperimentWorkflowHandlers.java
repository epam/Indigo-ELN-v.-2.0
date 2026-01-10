package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.SignatureStatus;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.service.mutation.AbstractExperimentMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.eln.model.ApplicationPermission.SUBMIT_EXPERIMENTS;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;

@Dependent
@MutationHandlerFor(ExperimentMutation.CancelExperiment.class)
class CancelExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CancelExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CancelExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.transition(experiment, CANCELLED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return new MutationResult("Experiment cancelled");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.CancelExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ReopenExperiment.class)
class ReopenExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ReopenExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.ReopenExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.transition(experiment, REOPEN, SUBMIT_EXPERIMENTS, CANCELLED, ARCHIVED, COMPLETED, SUBMITTED, REJECTED);
        experiment.getSignatures().clear();
        return new MutationResult("Experiment reopened");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.ReopenExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CompleteExperiment.class)
class CompleteExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CompleteExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.CompleteExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.transition(experiment, COMPLETED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return new MutationResult("Experiment completed");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.CompleteExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SubmitExperiment.class)
class SubmitExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.SubmitExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;
    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    SignatureTemplateRepository signatureTemplateRepository;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.SubmitExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        SignatureTemplateEntity signatureTemplate = signatureTemplateRepository.get(mutation.signatureTemplateID());
        helper.transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, COMPLETED);
        ExperimentService.ExperimentReportContent report = experimentService.printReport(experiment);
        AttachmentEntity attachment = attachmentService.createExperimentAttachment(experiment, report.filename(), report.content());
        experiment.setReportForSignature(attachment);
        experiment.getSignatures().clear();
        experiment.getSignatures().addAll(signatureTemplate.getBlocks().stream()
                .map(block -> {
                    UserEntity user = switch (block.getReason()) {
                        case WITNESS -> block.getUser();
                        case AUTHOR -> experiment.getCreatedBy();
                    };
                    return new ExperimentSignatureEntity(experiment, user, block.getReason(), null, null);
                })
                .toList()
        );
        helper.doCheckSignatures(experiment);
        return new MutationResult("Experiment submitted for signature");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.SubmitExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ApproveExperiment.class)
class ApproveExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ApproveExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.ApproveExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.doApproveOrReject(SignatureStatus.APPROVED, experiment);
        return new MutationResult("Experiment approved");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.ApproveExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.RejectExperiment.class)
class RejectExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.RejectExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.RejectExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.doApproveOrReject(SignatureStatus.REJECTED, experiment);
        return new MutationResult("Experiment rejected");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.RejectExperiment mutation, MutationContext context) {

    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ResubmitExperiment.class)
class ResubmitExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ResubmitExperiment> {

    @Inject
    ExperimentWorkflowHelper helper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutation.ResubmitExperiment mutation, MutationRedoInfo redoInfo, MutationContext context) {
        helper.transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, REJECTED);
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            signature.setStatus(null);
        }
        return new MutationResult("Experiment resubmitted for signature");
    }

    @Override
    public void initContext(ExperimentEntity experiment, ExperimentMutation.ResubmitExperiment mutation, MutationContext context) {

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
