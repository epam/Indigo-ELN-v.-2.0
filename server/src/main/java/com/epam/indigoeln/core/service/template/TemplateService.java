/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    /**
     * TemplateRepository instance for working with templates.
     */
    @Autowired
    private TemplateRepository templateRepository;

    /**
     * CustomDtoMapper instance for mapping.
     */
    @Autowired
    private CustomDtoMapper dtoMapper;

    private static final SortedPageUtil<Template> TEMPLATE_SORTED_PAGE_UTIL;

    static {
        Map<String, Function<Template, String>> functionMap = new HashMap<>();
        functionMap.put("name", Template::getName);
        functionMap.put("author", template -> template.getAuthor().getLogin());
        functionMap.put("creationDate", template -> template.getCreationDate().toInstant().toString());
        functionMap.put("lastEditDate", template -> template.getLastEditDate().toInstant().toString());

        TEMPLATE_SORTED_PAGE_UTIL = new SortedPageUtil<>(functionMap);
    }

    /**
     * Returns template by id.
     *
     * @param id Id
     * @return Template by id
     */
    public Optional<TemplateDTO> getTemplateById(String id) {
        return templateRepository.findById(id).map(TemplateDTO::new);
    }

    /**
     * Returns template by name.
     *
     * @param name Name
     * @return Template by name
     */
    public Optional<TemplateDTO> getTemplateByName(String name) {
        return templateRepository.findOneByName(name).map(TemplateDTO::new);
    }

    /**
     * Returns all templates.
     *
     * @param pageable Pagination information.
     * @return All templates
     */
    public Page<TemplateDTO> getAllTemplates(Pageable pageable) {
        return TEMPLATE_SORTED_PAGE_UTIL
                .getPage(templateRepository.findAll(), pageable)
                .map(TemplateDTO::new);
    }

    /**
     * Returns templates by name like.
     *
     * @param nameLike Name
     * @param pageable Pagination information
     * @return Templates
     */
    public Page<TemplateDTO> getTemplatesNameLike(String nameLike, Pageable pageable) {
        return TEMPLATE_SORTED_PAGE_UTIL
                .getPage(templateRepository.findByNameLikeIgnoreCase(nameLike), pageable)
                .map(TemplateDTO::new);
    }

    /**
     * Creates template.
     *
     * @param templateDTO Template transfer object
     * @return Created template
     */
    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        Template template = dtoMapper.convertFromDTO(templateDTO);
        Template savedTemplate = saveTemplateAndHandleError(template);
        return new TemplateDTO(savedTemplate);
    }

    /**
     * Updates template.
     *
     * @param templateDTO Template transfer object
     * @return Updated template
     */
    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = templateRepository.findById(templateDTO.getId()).
                orElseThrow(() -> EntityNotFoundException.createWithTemplateId(templateDTO.getId()));

        template.setName(templateDTO.getName());
        template.setTemplateContent(templateDTO.getTemplateContent());
        Template savedTemplate = templateRepository.save(template);
        return new TemplateDTO(savedTemplate);
    }

    /**
     * Removes template by id.
     *
     * @param templateId Template's id
     */
    public void deleteTemplate(String templateId) {
        templateRepository.deleteById(templateId);
    }

    private Template saveTemplateAndHandleError(Template template) {
        if (!nameAlreadyExists(template.getName())) {
            try {
                return templateRepository.save(template);
            } catch (DuplicateKeyException e) {
                throw DuplicateFieldException.createWithTemplateId(template.getName(), e);
            } catch (OptimisticLockingFailureException e) {
                throw ConcurrencyException.createWithTemplateName(template.getName(), e);
            }
        } else {
            throw DuplicateFieldException.createWithTemplateName();
        }
    }

    public boolean nameAlreadyExists(String name) {
        return templateRepository.existsByName(name);
    }
}
