package com.epam.indigoeln.core.service.template;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing Templates
 */
@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public Optional<TemplateDTO> getTemplateById(String id) {
        return Optional.ofNullable(templateRepository.findOne(id)).map(TemplateDTO::new);
    }

    public Optional<TemplateDTO> getTemplateByName(String name) {
        return templateRepository.findOneByName(name).map(TemplateDTO::new);
    }

    public Page<TemplateDTO> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable).map(TemplateDTO::new);
    }

    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        Template template = dtoMapper.convertFromDTO(templateDTO);
        Template savedTemplate = templateRepository.save(template);
        return new TemplateDTO(savedTemplate);
    }

    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = Optional.ofNullable(templateRepository.findOne(templateDTO.getId())).
                orElseThrow(() -> new EntityNotFoundException("Template with id does not exists", templateDTO.getId()));

        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);
        return new TemplateDTO(savedTemplate);
    }

    public void deleteTemplate(String templateId) {
        templateRepository.delete(templateId);
    }

}
