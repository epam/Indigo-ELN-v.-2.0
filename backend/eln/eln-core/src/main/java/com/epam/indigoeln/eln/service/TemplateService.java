package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

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
        TemplateEntity template = templateMapper.requestToTemplate(request);
        ModelUtil.updateDates(template, userService.getCurrentUser());
        templateRepository.persist(template);
        templateRepository.flushAndClear();
        return getTemplate(template.getId());
    }

    public Page<TemplateDTO> getTemplates(Paging paging) {
        var list = templateRepository.findAll(paging);
        return Page.of(paging, list.total(), list.list());
    }

    public TemplateDetailsDTO getTemplate(UUID templateId) {
        return templateRepository.loadDetails(templateId);
    }

    public TemplateDetailsDTO editTemplate(UUID templateId, TemplateEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_TEMPLATES);
        TemplateEntity template = templateRepository.get(templateId);
        editProperty(request.getName(), template::setName);
        ModelUtil.updateDates(template, userService.getCurrentUser());
        templateRepository.flushAndClear();
        return getTemplate(templateId);
    }
}
