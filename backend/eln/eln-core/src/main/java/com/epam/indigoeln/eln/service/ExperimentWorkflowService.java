package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.mapper.SignatureExperimentMapper;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
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
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.CancelExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO reopenExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.ReopenExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO completeExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.CompleteExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO submitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO completeAndSubmitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.CompleteExperiment());
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentForSignatureDTO approveOrRejectExperiment(UUID experimentId, boolean reject) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        if (reject) {
            doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.RejectExperiment());
        } else {
            doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.ApproveExperiment());
        }
        return signatureExperimentMapper.entityToDTO(experiment);
    }

    // TODO rework resubmit after Signature service is done; likely should just reuse "submit"
    public ExperimentDetailsDTO resubmitExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        doMutateModel(experiment, experiment.getModel(), new ExperimentMutation.ResubmitExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    // !!! see ExperimentService.doMutateModel
    private Pair<ExperimentModel, ExperimentModelPatch> doMutateModel(ExperimentEntity experiment, ExperimentModel model, Mutation mutation) {
        try {
            return experimentModelService.applyMutation(experiment, model, mutation);
        } catch (InvalidRequestException | EntityNotFoundException e) {
            throw e;
        } catch (Throwable e) {
            log.error("Failed to mutate model for experiment {}: {}", experiment.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to mutate model: " + e.getMessage(), e);
        }
    }
}
