package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.ProjectAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ProjectService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class ProjectResource implements ProjectAPI {

    @Inject
    ProjectService projectService;
    @Inject
    AttachmentService attachmentService;

    @Override
    public @NotNull @Valid ProjectDetailsDTO createProject(@NotNull @Valid ProjectRequest request) {
        return projectService.createProject(request);
    }

    @Override
    public @NotNull @Valid Page<ProjectDTO> getProjects(@Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, @Valid Paging paging) {
        return projectService.getProjects(search, sort, createdByMe, paging);
    }

    @Override
    public @NotNull @Valid ProjectExistenceCheckDTO checkProjectNameExistence(
            @NotEmpty String name){
        return projectService.checkExistenceByName(name);
    }

    @Override
    public @NotNull @Valid ProjectDetailsDTO getProject(@NotNull UUID projectId) {
        return projectService.getProject(projectId);
    }

    @Override
    public @NotNull @Valid ProjectDetailsDTO editProject(@NotNull UUID projectId, @NotNull @Valid ProjectEditRequest request) {
        return projectService.editProject(projectId, request);
    }

    @Override
    public String createProjectAttachment(UUID projectId, UploadForm form) {
        return attachmentService.createProjectAttachment(projectId, form.getFile(), true);
    }

    @Override
    public List<AttachmentDTO> completeProjectAttachment(UUID projectId) {
        return attachmentService.completeProjectAttachment(projectId);
    }

    @Override
    public Response downloadProjectAttachment(UUID projectId, UUID attachmentId) {
        return attachmentService.downloadProjectAttachment(projectId, attachmentId);
    }

    @Override
    public void deleteProjectAttachment(UUID projectId, UUID attachmentId) {
        attachmentService.deleteProjectAttachment(projectId, attachmentId);
    }

    @Override
    public List<ACLEntryDTO> updateProjectAccess(@NotNull UUID projectId, @NotNull @Valid List<AccessForm> form) {
        return projectService.updateProjectAccess(projectId, form);
    }

    @Override
    public List<NestedACLEntryDTO> getNestedProjectAccess(UUID projectId) {
        return projectService.getNestedProjectAccess(projectId);
    }

    @Override
    public List<RevisionSummaryDTO> getProjectRevisions(UUID projectId) {
        return projectService.getProjectRevisions(projectId);
    }
}
