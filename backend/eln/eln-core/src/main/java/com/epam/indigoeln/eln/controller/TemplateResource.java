package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.TemplateAPI;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.model.TemplateEditRequest;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.service.TemplateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class TemplateResource implements TemplateAPI {

    @Inject
    TemplateService templateService;

    @Override
    public @NotNull @Valid TemplateDetailsDTO createTemplate(@NotNull @Valid TemplateRequest request) {
        return templateService.createTemplate(request);
    }

    @Override
    public @NotNull @Valid Page<TemplateDTO> getTemplates(@Nullable String search,
                                                          @Nullable SortOrder sort,
                                                          @Nullable Boolean createdByMe,
                                                          @Valid Paging paging) {
        return templateService.getTemplates(search, sort, createdByMe, paging);
    }

    @Override
    public TemplateDetailsDTO getByName(String name) {
        return templateService.getByName(name);
    }

    @Override
    public @NotNull @Valid TemplateDetailsDTO getTemplate(@NotNull UUID templateId) {
        return templateService.getTemplate(templateId);
    }

    @Override
    public @NotNull @Valid TemplateDetailsDTO editTemplate(@NotNull UUID templateId, @NotNull @Valid TemplateEditRequest request) {
        return templateService.editTemplate(templateId, request);
    }

    @Override
    public void deleteTemplate(UUID templateId) {
        templateService.deleteTemplate(templateId);
    }
}
