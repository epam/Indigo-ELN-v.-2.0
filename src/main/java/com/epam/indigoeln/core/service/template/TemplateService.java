package com.epam.indigoeln.core.service.template;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.JsonUtil;

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
        Template template = templateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
    }

    public Optional<TemplateDTO> getTemplateByName(String name) {
        Template template = templateRepository.findOneByName(name);
        return Optional.ofNullable(template != null ? new TemplateDTO(template) : null);
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
        Template template = templateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(JsonUtil.basicDBListFromArray(templateDTO.getTemplateContent()));
        Template savedTemplate = templateRepository.save(template);
        return new TemplateDTO(savedTemplate);
    }

    public void deleteTemplate(String id) {
        templateRepository.delete(id);
    }

}
