package com.epam.indigoeln.signature.controller;


import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.signature.api.SignatureAPI;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.signature.entity.DocumentEntity;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateDetailsDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateRequest;
import com.epam.indigoeln.signature.service.SignatureService;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Path(SignatureAPI.BASE_PATH)
public class SignatureResource implements SignatureAPI {

    @Inject
    SignatureService service;

    @Inject
    @RestClient
    SignatureClient signatureClient;

    @Context
    RoutingContext routingContext;

    @Valid
    @Override
    public SignatureTemplateDetailsDTO createTemplate(@Valid SignatureTemplateRequest template) {
        return service.createTemplate(template);
    }

    @Override
    public List<@Valid SignatureTemplateDTO> getTemplates() {
        return service.getTemplates();
    }

    @Valid
    @Override
    @SneakyThrows
    public DocumentDTO uploadDocument(String name, UUID templateId, UploadForm form) {
        try {
            log.info("!!! uploadDocument");
            log.info("!!! uploadDocument: name={}", name);
            log.info("!!! uploadDocument: templateId={}", templateId);
            log.info("!!! uploadDocument: form={}", form);
            return service.createDocument(templateId, name, form.getFile().uploadedFile().toFile());
        } catch (Throwable e) {
            log.info("!!! uploadDocument: exception={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<@Valid DocumentDTO> getDocuments() {
        return service.getDocuments();
    }

    @Override
    public DocumentDTO getDocument(UUID id) {
        return service.getDocument(id);
    }

    @Valid
    @Override
    public DocumentDTO signDocument(UUID documentId) {
        return service.signOrRejectDocument(documentId, false);
    }

    @Valid
    @Override
    public DocumentDTO rejectDocument(UUID documentId) {
        return service.signOrRejectDocument(documentId, true);
    }

    @Override
    public Response downloadDocument(UUID documentId) {
        DocumentEntity document = service.getDocumentEntity(documentId);
        return Response.ok(document.getContent())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getName())
                .build();
    }
}
