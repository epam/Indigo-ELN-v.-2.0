package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.mapper.SignatureExperimentMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.reports.api.ReportsClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.eln.model.ApplicationPermission.SUBMIT_EXPERIMENTS;
import static com.epam.indigoeln.eln.model.ExperimentStatus.*;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class ExperimentWorkflowService {

    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;
    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    SignatureTemplateRepository signatureTemplateRepository;
    @Inject
    SignatureExperimentMapper signatureExperimentMapper;
    @Inject
    UserService userService;
    @Inject
    @RestClient
    ReportsClient reportsClient;
    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;

    public ExperimentDetailsDTO cancelExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        transition(experiment, CANCELLED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentDetailsDTO reopenExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        transition(experiment, REOPEN, SUBMIT_EXPERIMENTS, CANCELLED, ARCHIVED, COMPLETED, SUBMITTED, REJECTED);
        experiment.getSignatures().clear();
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentDetailsDTO completeExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doCompleteExperiment(experiment);
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentDetailsDTO submitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        SignatureTemplateEntity signatureTemplate = signatureTemplateRepository.get(signatureTemplateId);
        doSubmitExperiment(experiment, signatureTemplate);
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentDetailsDTO completeAndSubmitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        SignatureTemplateEntity signatureTemplate = signatureTemplateRepository.get(signatureTemplateId);
        doCompleteExperiment(experiment);
        doSubmitExperiment(experiment, signatureTemplate);
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentForSignatureDTO approveOrRejectExperiment(UUID experimentId, SignatureStatus status) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        boolean found = false;
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            if (signature.getUser().equals(userService.getCurrentUser())) {
                validate(signature.getStatus() == null, "Experiment was already approved or rejected by " + userService.getCurrentUser());
                signature.setStatus(status);
                found = true;
            }
        }
        validate(found, userService.getCurrentUser() + " is not listed as a signer of experiment " + experiment.getName());
        doCheckSignatures(experiment);
        return signatureExperimentMapper.entityToDTO(experiment);
    }

    public ExperimentDetailsDTO resubmitExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, REJECTED);
        for (ExperimentSignatureEntity signature : experiment.getSignatures()) {
            signature.setStatus(null);
        }
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    private void doCompleteExperiment(ExperimentEntity experiment) {
        transition(experiment, COMPLETED, SUBMIT_EXPERIMENTS, OPEN, REOPEN);
    }

    private void doSubmitExperiment(ExperimentEntity experiment, SignatureTemplateEntity signatureTemplate) {
        transition(experiment, SUBMITTED, SUBMIT_EXPERIMENTS, COMPLETED);
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
        doCheckSignatures(experiment);
    }

    private void doCheckSignatures(ExperimentEntity experiment) {
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

    private void transition(ExperimentEntity experiment, ExperimentStatus targetStatus, @Nullable ApplicationPermission requiredAccess, ExperimentStatus... allowedStatuses) {
        if (requiredAccess != null) {
            aclService.ensureAccess(experiment, requiredAccess);
        }
        ensureStatus(experiment, allowedStatuses);
        experiment.setStatus(targetStatus);
    }

    private void ensureStatus(ExperimentEntity experiment, ExperimentStatus... allowedStatuses) {
        if (!Arrays.asList(allowedStatuses).contains(experiment.getStatus())) {
            InvalidRequestException.fail("Experiment is " + experiment.getStatus() + ", must be " + StreamEx.of(allowedStatuses).joining(" or "));
        }
    }
}
