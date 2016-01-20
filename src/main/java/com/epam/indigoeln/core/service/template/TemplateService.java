package com.epam.indigoeln.core.service.template;

import com.epam.indigoeln.web.rest.dto.TemplateDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Templates
 */
public interface TemplateService {

    Optional<TemplateDTO> getTemplateById(String id);

    Optional<TemplateDTO> getTemplateByName(String name);

    List<TemplateDTO> getAllTemplates();

    TemplateDTO createTemplate(TemplateDTO template);

    TemplateDTO updateTemplate(TemplateDTO template);

    void deleteTemplate(String id);
}
