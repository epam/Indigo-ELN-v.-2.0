package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class ExperimentsResource implements ExperimentsAPI {

    @Inject
    ExperimentService experimentService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentModelService experimentModelService;

    @Override
    public @NotNull @Valid ExperimentDetailsDTO createExperiment(@NotNull UUID notebookId, @NotNull @Valid ExperimentRequest request) {
        return experimentService.createExperiment(notebookId, request);
    }

    @Override
    public @NotNull @Valid ExperimentDetailsDTO getExperiment(@NotNull UUID experimentId) {
        return experimentService.getExperiment(experimentId);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getProjectExperiments(@NotNull UUID projectId, @Valid Paging paging) {
        return experimentService.getExperiments(projectId, null, paging);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getNotebookExperiments(@NotNull UUID notebookId, @Valid Paging paging) {
        return experimentService.getExperiments(null, notebookId, paging);
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
    public @NotNull Boolean markExperiment(@NotNull UUID experimentId) {
        return experimentService.markExperiment(experimentId, true);
    }

    @Override
    public @NotNull Boolean unmarkExperiment(@NotNull UUID experimentId) {
        return experimentService.markExperiment(experimentId, false);
    }

    @Override
    public List<ACLEntryDTO> updateExperimentAccess(@NotNull UUID experimentId, @NotNull @Valid List<AccessForm> form) {
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
}
