package com.epam.indigoeln.core.service.template;

import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.service.exception.ConcurrencyException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.util.SortedPageUtil;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service class for managing Templates.
 */
@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    private static final SortedPageUtil<Template> templateSortedPageUtil;

    static {
        Map<String, Function<Template, String>> functionMap = new HashMap<>();
        functionMap.put("name", Template::getName);
        functionMap.put("author", template -> template.getAuthor().getLogin());
        functionMap.put("creationDate", template -> template.getCreationDate().toInstant().toString());
        functionMap.put("lastEditDate", template -> template.getLastEditDate().toInstant().toString());

        templateSortedPageUtil = new SortedPageUtil<>(functionMap);
    }

    public Optional<TemplateDTO> getTemplateById(String id) {
        return Optional.ofNullable(templateRepository.findOne(id)).map(TemplateDTO::new);
    }

    public Optional<TemplateDTO> getTemplateByName(String name) {
        return templateRepository.findOneByName(name).map(TemplateDTO::new);
    }

    public Page<TemplateDTO> getAllTemplates(Pageable pageable) {
        return templateSortedPageUtil
                .getPage(templateRepository.findAll(), pageable)
                .map(TemplateDTO::new);
    }

    public Page<TemplateDTO> getTemplatesNameLike(String nameLike, Pageable pageable) {
        return templateSortedPageUtil
                .getPage(templateRepository.findByNameLikeIgnoreCase(nameLike), pageable)
                .map(TemplateDTO::new);
    }

    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        Template template = dtoMapper.convertFromDTO(templateDTO);
        Template savedTemplate = saveTemplateAndHandleError(template);
        return new TemplateDTO(savedTemplate);
    }

    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = Optional.ofNullable(templateRepository.findOne(templateDTO.getId())).
                orElseThrow(() -> EntityNotFoundException.createWithTemplateId(templateDTO.getId()));

        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);
        return new TemplateDTO(savedTemplate);
    }

    public void deleteTemplate(String templateId) {
        templateRepository.delete(templateId);
    }

    private Template saveTemplateAndHandleError(Template template) {
        try {
            return templateRepository.save(template);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithTemplateName(template.getName(), e);
        } catch (OptimisticLockingFailureException e) {
            throw ConcurrencyException.createWithTemplateName(template.getName(), e);
        }
    }
}