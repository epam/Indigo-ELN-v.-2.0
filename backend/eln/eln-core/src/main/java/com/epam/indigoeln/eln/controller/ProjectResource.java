package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.ProjectAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.ProjectService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
    public @NotNull @Valid Page<ProjectDTO> getProjects(@Nullable String search, @Valid Paging paging) {
        return projectService.getProjects(search, paging);
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
    public List<AttachmentDTO> createProjectAttachment(UUID projectId, UploadForm form) {
        return attachmentService.createProjectAttachment(projectId, form.getFile());
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
    public List<ACLDetailsEntryDTO> updateProjectAccess(@NotNull UUID projectId, @NotNull @Valid List<AccessForm> form) {
        return projectService.updateProjectAccess(projectId, form);
    }
}
