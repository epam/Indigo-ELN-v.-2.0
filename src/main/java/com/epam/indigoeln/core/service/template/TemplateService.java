package com.epam.indigoeln.core.service.template;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.epam.indigoeln.core.model.ComponentTemplate;
import com.epam.indigoeln.core.repository.template.component.ComponentTemplateRepository;
import com.epam.indigoeln.web.rest.dto.ComponentTemplateDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.ExperimentTemplate;
import com.epam.indigoeln.core.repository.template.experiment.ExperimentTemplateRepository;
import com.epam.indigoeln.web.rest.dto.ExperimentTemplateDTO;

/**
 * Service class for managing Templates
 */
@Service
public class TemplateService {

    @Autowired
    private ExperimentTemplateRepository expTemplateRepository;

    @Autowired
    private ComponentTemplateRepository compTemplateRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public Optional<ExperimentTemplateDTO> getTemplateById(String id) {
        ExperimentTemplate template = expTemplateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new ExperimentTemplateDTO(template) : null);
    }

    public Optional<ExperimentTemplateDTO> getTemplateByName(String name) {
        ExperimentTemplate template = expTemplateRepository.findOneByName(name);
        return Optional.ofNullable(template != null ? new ExperimentTemplateDTO(template) : null);
    }

    public Page<ExperimentTemplateDTO> getAllTemplates(Pageable pageable) {
        return expTemplateRepository.findAll(pageable).map(ExperimentTemplateDTO::new);
    }

    public ExperimentTemplateDTO createTemplate(ExperimentTemplateDTO templateDTO) {
        ExperimentTemplate template = dtoMapper.convertFromDTO(templateDTO);
        template.setComponents(saveComponents(templateDTO.getComponents()));
        ExperimentTemplate savedTemplate = expTemplateRepository.save(template);
        return new ExperimentTemplateDTO(savedTemplate);
    }

    public ExperimentTemplateDTO updateTemplate(ExperimentTemplateDTO templateDTO) {
        ExperimentTemplate template = expTemplateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setComponents(saveComponents(templateDTO.getComponents()));
        ExperimentTemplate savedTemplate = expTemplateRepository.save(template);
        return new ExperimentTemplateDTO(savedTemplate);
    }

    public void deleteTemplate(String id) {
        expTemplateRepository.delete(id);
    }


    public Optional<ComponentTemplateDTO> getComponentTemplateById(String id) {
        ComponentTemplate template = compTemplateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new ComponentTemplateDTO(template) : null);
    }

    public Page<ComponentTemplateDTO> getAllComponentTemplates(Pageable pageable) {
        return compTemplateRepository.findAll(pageable).map(ComponentTemplateDTO::new);
    }

    public ComponentTemplateDTO createComponentTemplate(ComponentTemplateDTO templateDTO) {
        ComponentTemplate template = dtoMapper.convertFromDTO(templateDTO);
        return new ComponentTemplateDTO(compTemplateRepository.save(template));
    }

    public ComponentTemplateDTO updateComponentTemplate(ComponentTemplateDTO templateDTO) {
        ComponentTemplate template = compTemplateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        return new ComponentTemplateDTO(compTemplateRepository.save(template));
    }

    public void deleteComponentTemplate(String id) {
        compTemplateRepository.delete(id);
    }

    private List<ComponentTemplate> saveComponents(List<ComponentTemplateDTO> dtoList) {
        return dtoList.stream().map(dto -> saveComponent(dto)).collect(Collectors.toList());
    }

    private ComponentTemplate saveComponent(ComponentTemplateDTO dto) {
        ComponentTemplate result;
        if(dto.getId() == null) {
            result = dtoMapper.convertFromDTO(dto);
        } else {
            result = compTemplateRepository.findOne(dto.getId());
            result.setName(dto.getName());
            result.setTemplateContent(dto.getTemplateContent());
        }
        return compTemplateRepository.save(result);
    }
}
