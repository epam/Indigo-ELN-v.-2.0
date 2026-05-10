package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.service.ExperimentWorkflowService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
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
    public @NotNull @Valid ExperimentSnapshot getExperimentSnapshot(@NotNull UUID experimentId) {
        return experimentService.getExperimentSnapshot(experimentId);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getProjectExperiments(@NotNull UUID projectId, @Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, @Valid Paging paging) {
        return experimentService.getExperiments(projectId, null, search, sort, createdByMe, paging);
    }

    @Override
    public @NotNull @Valid Page<ExperimentDTO> getNotebookExperiments(@NotNull UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, @Valid Paging paging) {
        return experimentService.getExperiments(null, notebookId, search, sort, createdByMe, paging);
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
        return attachmentService.createExperimentAttachment(experimentId, form.getFile(), true);
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
    public byte[] getExperimentPicture(UUID experimentId, @Nullable Integer revision) {
        // revision is only used to prevent browser from using cached version when revision change
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
    public ExperimentModel mutateExperimentModel(UUID experimentId, MutateModelForm modelAndMutation) {
        return experimentService.mutateModel(experimentId, modelAndMutation.getMutation());
    }

    @Override
    public ExperimentSnapshot mutateExperimentModel3(UUID experimentId, Integer revision, Mutation mutation) {
        return experimentService.mutateModel3(experimentId, revision, mutation);
    }

    @Override
    public MutationResponse mutateExperimentModel4(UUID experimentId, Integer revision, Mutation mutation) {
        return experimentService.mutateModel4(experimentId, revision, mutation);
    }

    @Override
    public JsonNode mutateExperimentModel2(UUID experimentId, Integer revision, Mutation mutation) {
        return experimentService.mutateModel2(experimentId, revision, mutation);
    }

    @Override
    public Map<InputAnchor, String> analyzeRXN(UUID experimentId, ReactionAnchor reactionAnchor) {
        return experimentService.analyzeRXN(experimentId, reactionAnchor);
    }

    @Override
    public byte[] getReactionPicture(UUID experimentId, ReactionAnchor reactionAnchor, @Nullable Integer revision) {
        // revision is only used to prevent browser from using cached version when revision change
        return experimentService.getReactionPicture(experimentId, reactionAnchor);
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
    public List<SignatureTemplateRef> getSignatureTemplates() {
        return experimentWorkflowService.getSignatureTemplates();
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
    public Response printReport(UUID experimentId) {
        return experimentService.printReport(experimentId);
    }

    @Override
    public List<RevisionDetailsDTO> getExperimentRevisions(UUID experimentId, @Nullable UUID editSessionId, @Nullable Boolean reverseOrder) {
        return experimentService.getExperimentRevisions(experimentId, editSessionId, reverseOrder);
    }

    @Override
    public List<ExperimentRevisionSummaryDTO> getExperimentRevisionsSummary(UUID experimentId) {
        return experimentService.getExperimentRevisionsSummary(experimentId);
    }

    @Override
    public JsonNode compareVersions(UUID experimentId, @Nullable Integer versionFrom, @Nullable Integer versionTo) {
        return experimentService.compareVersions(experimentId, versionFrom, versionTo);
    }

    @Override
    public String compareVersionsHTML(UUID experimentId, @org.jspecify.annotations.Nullable Integer versionFrom, @org.jspecify.annotations.Nullable Integer versionTo) {
        return experimentService.compareVersionsHTML(experimentId, versionFrom, versionTo);
    }

    @Override
    public List<ExperimentRef> suggestExperiments(String search) {
        return experimentService.suggestExperiments(search);
    }
}
