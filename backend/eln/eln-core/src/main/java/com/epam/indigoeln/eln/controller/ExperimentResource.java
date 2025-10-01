package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.service.ExperimentWorkflowService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Path(BaseAPI.BASE_PATH)
public class ExperimentResource implements ExperimentAPI {

    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentWorkflowService experimentWorkflowService;

    @Override
    public @NotNull @Valid ExperimentDetailsDTO createExperiment(@NotNull UUID notebookId, @NotNull @Valid ExperimentRequest request) {
        return experimentService.createExperiment(notebookId, request);
    }

    @Override
    public @NotNull @Valid ExperimentDetailsDTO getExperiment(@NotNull UUID experimentId) {
        return experimentService.getExperiment(experimentId);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getProjectExperiments(@NotNull UUID projectId, @Nullable SortOrder sort, @Nullable Boolean createdByMe, @Valid Paging paging) {
        return experimentService.getExperiments(projectId, null, sort, createdByMe, paging);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getNotebookExperiments(@NotNull UUID notebookId, @Nullable SortOrder sort, @Nullable Boolean createdByMe, @Valid Paging paging) {
        return experimentService.getExperiments(null, notebookId, sort, createdByMe, paging);
    }

    @Override
    public List<ExperimentDTO> getMarkedExperiments() {
        return experimentService.getMarkedExperiments();
    }

    @Override
    public @NotNull @Valid ExperimentDetailsDTO editExperiment(@NotNull UUID experimentId, @NotNull @Valid ExperimentEditRequest request) {
        return experimentService.editExperiment(experimentId, request);
    }

    @Override
    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, UploadForm form) {
        return attachmentService.createExperimentAttachment(experimentId, form.getFile());
    }

    @Override
    public Response downloadExperimentAttachment(UUID experimentId, UUID attachmentId) {
        return attachmentService.downloadExperimentAttachment(experimentId, attachmentId);
    }

    @Override
    public void deleteExperimentAttachment(UUID experimentId, UUID attachmentId) {
        attachmentService.deleteExperimentAttachment(experimentId, attachmentId);
    }

    @Override
    public byte[] getExperimentPicture(UUID experimentId) {
        return experimentService.getExperimentPicture(experimentId);
    }

    @Override
    public @NotNull Boolean markExperiment(@NotNull UUID experimentId) {
        return experimentService.markExperiment(experimentId, true);
    }

    @Override
    public @NotNull Boolean unmarkExperiment(@NotNull UUID experimentId) {
        return experimentService.markExperiment(experimentId, false);
    }

    @Override
    public List<ACLDetailsEntryDTO> updateExperimentAccess(@NotNull UUID experimentId, @NotNull @Valid List<AccessForm> form) {
        return experimentService.updateExperimentAccess(experimentId, form);
    }

    @Override
    public @Valid ExperimentModel getExperimentModel(@NotNull UUID experimentId) {
        return experimentService.getModel(experimentId);
    }

    @Override
    public ExperimentModel mutateExperimentModel(UUID experimentId, MutateModelForm modelAndMutation) {
        return experimentService.mutateModel(experimentId, modelAndMutation.getModel(), modelAndMutation.getMutation());
    }

    @Override
    public ExperimentDetailsDTO cancelExperiment(UUID experimentId) {
        return experimentWorkflowService.cancelExperiment(experimentId);
    }

    @Override
    public ExperimentDetailsDTO reopenExperiment(UUID experimentId) {
        return experimentWorkflowService.reopenExperiment(experimentId);
    }

    @Override
    public ExperimentDetailsDTO completeExperiment(UUID experimentId) {
        return experimentWorkflowService.completeExperiment(experimentId);
    }

    @Override
    public ExperimentDetailsDTO submitExperiment(UUID experimentId, UUID signatureTemplateId) {
        return experimentWorkflowService.submitExperiment(experimentId, signatureTemplateId);
    }

    @Override
    public ExperimentDetailsDTO completeAndSubmitExperiment(UUID experimentId, UUID signatureTemplateId) {
        return experimentWorkflowService.completeAndSubmitExperiment(experimentId, signatureTemplateId);
    }

    @Override
    public ExperimentForSignatureDTO approveExperiment(UUID experimentId) {
        return experimentWorkflowService.approveOrRejectExperiment(experimentId, SignatureStatus.APPROVED);
    }

    @Override
    public ExperimentForSignatureDTO rejectExperiment(UUID experimentId) {
        return experimentWorkflowService.approveOrRejectExperiment(experimentId, SignatureStatus.REJECTED);
    }

    @Override
    public ExperimentDetailsDTO resubmitExperiment(UUID experimentId) {
        return experimentWorkflowService.resubmitExperiment(experimentId);
    }

    @Override
    public Response printReport(UUID experimentId) {
        return experimentService.printReport(experimentId);
    }
}
