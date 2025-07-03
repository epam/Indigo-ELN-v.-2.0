package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.TemplateAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.*;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class TemplateResource implements TemplateAPI {

    @Inject
    ProjectService projectService;
    @Inject
    NotebookService notebookService;
    @Inject
    ExperimentService experimentService;
    @Inject
    TemplateService templateService;
    @Inject
    AttachmentService attachmentService;
    @Inject
    UserService userService;
    @Inject
    SupportService supportService;
    @Inject
    DictionaryService dictionaryService;

    @Override
    public @NotNull @Valid TemplateDetailsDTO createTemplate(@NotNull @Valid TemplateRequest request) {
        return templateService.createTemplate(request);
    }

    @Override
    public @NotNull @Valid Page<TemplateDTO> getTemplates(@Valid Paging paging) {
        return templateService.getTemplates(paging);
    }

    @Override
    public @NotNull @Valid TemplateDetailsDTO getTemplate(@NotNull UUID templateId) {
        return templateService.getTemplate(templateId);
    }

    @Override
    public @NotNull @Valid TemplateDetailsDTO editTemplate(@NotNull UUID templateId, @NotNull @Valid TemplateEditRequest request) {
        return templateService.editTemplate(templateId, request);
    }
}
