package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.TraceSegment;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.SignatureExperimentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@DataAccess
@TraceSegment
@Transactional
@ApplicationScoped
public class SignatureExperimentService {

    @Inject
    SignatureExperimentRepository signatureExperimentRepository;
    @Inject
    ExperimentRepository experimentRepository;

    public Page<ExperimentForSignatureDTO> getExperimentsForSignature(Paging paging) {
        return signatureExperimentRepository.findAll(paging);
    }

    public Response downloadReportForSignature(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        if (experiment.getReportForSignature() == null) {
            throw new InvalidRequestException("Experiment doesn't have report for signature");
        }
        return Response.ok(experiment.getReportForSignature().getContent())
                .header("Content-Disposition", "attachment; filename=\"" + experiment.getReportForSignature().getName() + "\"")
                .header("Content-Type", "application/pdf")
                .build();
    }
}
