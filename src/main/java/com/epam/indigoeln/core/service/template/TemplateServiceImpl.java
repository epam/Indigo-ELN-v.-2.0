package com.epam.indigoeln.core.service.template;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.security.AuthoritiesConstants;

/**
 * Service class for managing Templates
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);

    @Autowired
    TemplateRepository templateRepository;

    @Override
    public Optional<TemplateDTO> getTemplateById(String id) {
        Template template = templateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
    }

    @Override
    public Optional<TemplateDTO> getTemplateByName(String name) {
        Template template = templateRepository.findOneByName(name);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
    }

    @Override
    public List<TemplateDTO> getAllTemplates() {
        List<Template> templateList = templateRepository.findAll();
        return templateList.stream().map(TemplateDTO::new).collect(Collectors.toList());
    }

    @Override
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

    @Override
    @Secured(AuthoritiesConstants.ADMIN)
    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = templateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);

        log.debug("Updated Information for Template: {}", savedTemplate);
        return new TemplateDTO(savedTemplate);
    }

    @Override
    @Secured(AuthoritiesConstants.ADMIN)
    public void deleteTemplate(String id) {
        templateRepository.delete(id);
        log.debug("user with id {} deleted", id);
    }
}
