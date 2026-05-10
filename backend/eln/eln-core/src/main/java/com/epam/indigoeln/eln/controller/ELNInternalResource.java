package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.api.ELNInternalAPI;
import com.epam.indigoeln.eln.service.ExperimentWorkflowService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Path(ELNInternalAPI.BASE_PATH)
public class ELNInternalResource implements ELNInternalAPI {

    @Inject
    ExperimentWorkflowService experimentWorkflowService;

    @Override
    public void internalSignatureUpdated(UUID documentId, String message, DocumentStatus documentStatus) {
        experimentWorkflowService.signatureUpdated(documentId, message, documentStatus);
    }
}
