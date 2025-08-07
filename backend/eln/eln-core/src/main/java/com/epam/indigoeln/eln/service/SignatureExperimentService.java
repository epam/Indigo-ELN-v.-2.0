package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.repository.SignatureExperimentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class SignatureExperimentService {

    @Inject
    SignatureExperimentRepository signatureExperimentRepository;

    public Page<ExperimentForSignatureDTO> getExperimentsForSignature(@NotNull @Valid Paging paging) {
        return signatureExperimentRepository.findAll(paging);
    }
}
