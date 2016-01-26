package com.epam.indigoeln.core.service.template;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing Templates
 */
@Service
public class TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    @Autowired
    private TemplateRepository templateRepository;

    public Optional<TemplateDTO> getTemplateById(String id) {
        Template template = templateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
    }

    public Optional<TemplateDTO> getTemplateByName(String name) {
        Template template = templateRepository.findOneByName(name);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
    }

    public Page<Template> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    @Secured(AuthoritiesConstants.ADMIN)
    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        Template template = new Template();
        template.setId(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);

        log.debug("Created Information for Template: {}", savedTemplate);
        return new TemplateDTO(savedTemplate);
    }

    @Secured(AuthoritiesConstants.ADMIN)
    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = templateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);

        log.debug("Updated Information for Template: {}", savedTemplate);
        return new TemplateDTO(savedTemplate);
    }

    @Secured(AuthoritiesConstants.ADMIN)
    public void deleteTemplate(String id) {
        templateRepository.delete(id);
        log.debug("user with id {} deleted", id);
    }
}
