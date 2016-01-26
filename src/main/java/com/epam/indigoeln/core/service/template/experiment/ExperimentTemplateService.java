package com.epam.indigoeln.core.service.template.experiment;
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
public class ExperimentTemplateService {

    @Autowired
    private ExperimentTemplateRepository experimentTemplateRepository;

    @Autowired
    private ComponentTemplateRepository componentTemplateRepository;

    @Autowired
    private CustomDtoMapper mapper;

    public Optional<ExperimentTemplateDTO> getTemplateById(String id) {
        ExperimentTemplate template = experimentTemplateRepository.findOne(id);
        return Optional.ofNullable(template != null ? new ExperimentTemplateDTO(template) : null);
    }

    public Optional<ExperimentTemplateDTO> getTemplateByName(String name) {
        ExperimentTemplate template = experimentTemplateRepository.findOneByName(name);
        return Optional.ofNullable(template != null ? new ExperimentTemplateDTO(template) : null);
    }

    public Page<ExperimentTemplateDTO> getAllTemplates(Pageable pageable) {
        return experimentTemplateRepository.findAll(pageable).map(ExperimentTemplateDTO::new);
    }

    public ExperimentTemplateDTO createTemplate(ExperimentTemplateDTO templateDTO) {
        ExperimentTemplate template = mapper.convertFromDTO(templateDTO);
        template.setComponents(saveComponents(templateDTO.getComponents()));
        ExperimentTemplate savedTemplate = experimentTemplateRepository.save(template);
        return new ExperimentTemplateDTO(savedTemplate);
    }

    public ExperimentTemplateDTO updateTemplate(ExperimentTemplateDTO templateDTO) {
        ExperimentTemplate template = experimentTemplateRepository.findOne(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setComponents(saveComponents(templateDTO.getComponents()));
        ExperimentTemplate savedTemplate = experimentTemplateRepository.save(template);
        return new ExperimentTemplateDTO(savedTemplate);
    }

    public void deleteTemplate(String id) {
        experimentTemplateRepository.delete(id);
    }

    private List<ComponentTemplate> saveComponents(List<ComponentTemplateDTO> dtoList) {
        return dtoList.stream().map(dto -> saveComponent(dto)).collect(Collectors.toList());
    }

    private ComponentTemplate saveComponent(ComponentTemplateDTO dto) {
        ComponentTemplate result;
        if(dto.getId() == null) {
            result = mapper.convertFromDTO(dto);
        } else {
            result = componentTemplateRepository.findOne(dto.getId());
            result.setName(dto.getName());
            result.setTemplateContent(dto.getTemplateContent());
        }
        return componentTemplateRepository.save(result);
    }
}
