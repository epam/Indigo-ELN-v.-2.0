package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
import com.epam.indigoeln.eln.util.TemplateValidationUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@DataAccess
@Transactional
@ApplicationScoped
public class TemplateService {

    @Inject
    TemplateRepository templateRepository;
    @Inject
    TemplateMapper templateMapper;
    @Inject
    UserService userService;
    @Inject
    ACLService aclService;

    public TemplateDetailsDTO createTemplate(TemplateRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_TEMPLATES);

        TemplateValidationUtil.validateTemplateRequest(request, templateRepository);

        TemplateEntity template = templateMapper.requestToTemplate(request);
        updateDates(template, userService.getCurrentUser());

        try {
            templateRepository.persist(template);
            templateRepository.flushAndClear();
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if ("template_name_uq".equals(e.getConstraintName())) {
                throw new InvalidRequestException("Template with name '" + request.getName() + "' already exists.");
            }
            throw e;
        }

        return getTemplate(template.getId());
    }

    public Page<TemplateDTO> getTemplates(Paging paging) {
        return templateRepository.findAll(paging);
    }

    public TemplateDetailsDTO getTemplate(UUID templateId) {
        return templateRepository.loadDetails(templateId);
    }

    public TemplateDetailsDTO editTemplate(UUID templateId, TemplateEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_TEMPLATES);

        TemplateEntity template = templateRepository.get(templateId);

        editProperty(request.getName(), template::setName);

        updateDates(template, userService.getCurrentUser());

        templateRepository.flushAndClear();

        return getTemplate(templateId);
    }
}
