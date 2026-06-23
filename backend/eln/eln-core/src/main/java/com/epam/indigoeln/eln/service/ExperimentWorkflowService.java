package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateRef;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.signature.api.SignatureClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

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
    UserService userService;
    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    @RestClient
    SignatureClient signatureClient;

    public ExperimentDetailsDTO cancelExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.getAndLock(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CancelExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO reopenExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.getAndLock(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.ReopenExperiment());
        return experimentService.getExperimentDetails(experiment);
    }

    public List<SignatureTemplateRef> getSignatureTemplates() {
        return signatureClient.getTemplates().stream()
                .map(t -> new SignatureTemplateRef(t.getId(), t.getName()))
                .toList();
    }

    public ExperimentDetailsDTO completeExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.getAndLock(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CompleteExperiment());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.MakeVersion());
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO submitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.getAndLock(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    public ExperimentDetailsDTO completeAndSubmitExperiment(UUID experimentId, UUID signatureTemplateId) {
        ExperimentEntity experiment = experimentRepository.getAndLock(experimentId);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CompleteExperiment());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.MakeVersion());
        experimentModelService.applyMutation(experiment, new ExperimentMutation.SubmitExperiment(signatureTemplateId));
        return experimentService.getExperimentDetails(experiment);
    }

    @SneakyThrows
    public void signatureUpdated(UUID documentId, String message, DocumentStatus updatedStatus, Path path) {
        ExperimentEntity experiment = experimentRepository.findBySignatureNumber(documentId.toString());
        AttachmentEntity submittedAttachment = checkNotNull(experiment.getSignatureAttachment());
        byte[] bytes = Files.readAllBytes(path);
        AttachmentEntity attachment = attachmentService.createExperimentAttachment(experiment, submittedAttachment.getName(), bytes, null);
        ExperimentMutation mutation = new ExperimentMutation.SignatureUpdated(message, updatedStatus, attachment.getId());
        experimentModelService.applyMutation(experiment, mutation);
    }
}
