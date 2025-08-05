package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.SignatureTemplateAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.SignatureTemplateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class SignatureTemplateResource implements SignatureTemplateAPI {

    @Inject
    SignatureTemplateService signatureTemplateService;

    @Override
    public SignatureTemplateDetailsDTO createSignatureTemplate(@Valid @NotNull SignatureTemplateRequest request) {
        return signatureTemplateService.createSignatureTemplate(request);
    }

    @Override
    public Page<SignatureTemplateDTO> getSignatureTemplates(@Valid @NotNull Paging paging) {
        return signatureTemplateService.getSignatureTemplates(paging);
    }

    @Override
    public SignatureTemplateDetailsDTO getSignatureTemplate(@NotNull UUID signatureTemplateId) {
        return signatureTemplateService.getSignatureTemplate(signatureTemplateId);
    }

    @Override
    public SignatureTemplateDetailsDTO editSignatureTemplate(@NotNull UUID signatureTemplateId, @Valid @NotNull SignatureTemplateEditRequest request) {
        return signatureTemplateService.editSignatureTemplate(signatureTemplateId, request);
    }
}
