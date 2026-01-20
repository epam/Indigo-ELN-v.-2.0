package com.epam.indigoeln.signature.controller;


import com.epam.indigoeln.signature.api.SignatureAPI;
import com.epam.indigoeln.signature.entity.DocumentEntity;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.Document;
import com.epam.indigoeln.signature.model.Template;
import com.epam.indigoeln.signature.model.TemplateRequest;
import com.epam.indigoeln.signature.service.SignatureService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path(SignatureAPI.BASE_PATH)
public class SignatureResource implements SignatureAPI {

    @Inject
    SignatureService service;
    @Inject
    SignatureMapper mapper;

    @Override
    public @Valid Template createTemplate(@Valid TemplateRequest template) {
        return service.createTemplate(template);
    }

    @Override
    public List<@Valid Template> getTemplates() {
        return service.getTemplates().stream().map(mapper::entityToTemplate).toList();
    }

    @Override
    public @Valid Document uploadDocument(FileUploadForm form) {
        return service.createDocument(form.getTemplateId(), form.getName(), form.getFile());
    }

    @Override
    public List<@Valid Document> getDocuments() {
        return service.getDocuments();
    }

    @Override
    public @Valid Document signDocument(int documentId, @Valid SignForm form) {
        return service.signOrRejectDocument(documentId, false, form.getKeyStore(), form.getKeyStorePassword());
    }

    @Override
    public @Valid Document rejectDocument(int documentId) {
        return service.signOrRejectDocument(documentId, true, null, null);}

    @Override
    public Response downloadDocument(int documentId) {
        DocumentEntity document = service.getDocument(documentId);
        return Response.ok(document.getContent())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getName())
                .build();
    }
}
