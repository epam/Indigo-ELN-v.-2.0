package com.epam.indigoeln.core.service.template.component;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.ComponentTemplate;
import com.epam.indigoeln.core.repository.template.component.ComponentTemplateRepository;
import com.epam.indigoeln.web.rest.dto.ComponentTemplateDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;


@Service
public class ComponentTemplateService {

    @Autowired
    private ComponentTemplateRepository repository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public Optional<ComponentTemplateDTO> getTemplateById(String id) {
        ComponentTemplate template = repository.findOne(id);
        return Optional.ofNullable(template != null ? new ComponentTemplateDTO(template) : null);
    }

    public Page<ComponentTemplateDTO> getAllTemplates(Pageable pageable) {
        return repository.findAll(pageable).map(ComponentTemplateDTO::new);
    }

    public ComponentTemplateDTO createTemplate(ComponentTemplateDTO templateDTO) {
        ComponentTemplate template = dtoMapper.convertFromDTO(templateDTO);
        return new ComponentTemplateDTO(repository.save(template));
    }

    public ComponentTemplateDTO updateTemplate(ComponentTemplateDTO templateDTO) {
        ComponentTemplate template = repository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        return new ComponentTemplateDTO(repository.save(template));
    }

    public void deleteTemplate(String id) {
        repository.delete(id);
    }
}
