package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.SignatureTemplateBlockEmbedded;
import com.epam.indigoeln.eln.entity.SignatureTemplateEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.SignatureTemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.SignatureTemplateRepository;
import com.epam.indigoeln.eln.repository.UserRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@DataAccess
@Transactional
@ApplicationScoped
public class SignatureTemplateService {

    @Inject
    SignatureTemplateRepository signatureTemplateRepository;
    @Inject
    SignatureTemplateMapper signatureTemplateMapper;
    @Inject
    UserService userService;
    @Inject
    ACLService aclService;
    @Inject
    UserRepository userRepository;

    public SignatureTemplateDetailsDTO createSignatureTemplate(SignatureTemplateRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_TEMPLATES);
        SignatureTemplateEntity template = signatureTemplateMapper.requestToTemplate(request);
        updateBlocks(template, request.getBlocks());
        ModelUtil.updateDates(template, userService.getCurrentUser());
        signatureTemplateRepository.persist(template);
        signatureTemplateRepository.flushAndClear();
        return getSignatureTemplate(template.getId());
    }

    public Page<SignatureTemplateDTO> getSignatureTemplates(Paging paging) {
        return signatureTemplateRepository.findAll(paging);
    }

    public SignatureTemplateDetailsDTO getSignatureTemplate(UUID templateId) {
        return signatureTemplateRepository.loadDetails(templateId);
    }

    public SignatureTemplateDetailsDTO editSignatureTemplate(UUID templateId, SignatureTemplateEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_TEMPLATES);
        SignatureTemplateEntity template = signatureTemplateRepository.get(templateId);
        editProperty(request.getName(), template::setName);
        editProperty(request.getBlocks(), blocks -> updateBlocks(template, blocks));
        ModelUtil.updateDates(template, userService.getCurrentUser());
        signatureTemplateRepository.flushAndClear();
        return getSignatureTemplate(templateId);
    }

    private void updateBlocks(SignatureTemplateEntity template, List<SignatureBlock> blocks) {
        template.setBlocks(blocks.stream()
                .map(block -> {
                    UserEntity user = block.getUser() != null ? userRepository.get(block.getUser().getId()) : null;
                    return new SignatureTemplateBlockEmbedded(user, block.getReason());
                })
                .toList()
        );
    }
}
