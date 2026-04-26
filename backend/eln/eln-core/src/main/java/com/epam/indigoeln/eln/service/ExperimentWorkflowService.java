package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.mapper.SignatureExperimentMapper;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

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
    SignatureTemplateRepository signatureTemplateRepository;
    @Inject
    SignatureExperimentMapper signatureExperimentMapper;
    @Inject
    UserService userService;
    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentModelService experimentModelService;

    public ExperimentDetailsDTO cancelExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CancelExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO reopenExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.ReopenExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO completeExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CompleteExperiment());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.MakeVersion());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO submitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO completeAndSubmitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CompleteExperiment());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.MakeVersion());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentForSignatureDTO approveOrRejectExperiment(UUID experimentId, boolean reject) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        Mutation mutation = reject ? new ExperimentMutation.RejectExperiment() : new ExperimentMutation.ApproveExperiment();
        experimentModelService.applyMutation(experiment, mutation);
        return signatureExperimentMapper.entityToDTO(experiment);
    }

    // TODO rework resubmit after Signature service is done; likely should just reuse "submit"
    public ExperimentDetailsDTO resubmitExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.ResubmitExperiment());
        return experimentService.getExperimentDetails(experiment);
    }
}
