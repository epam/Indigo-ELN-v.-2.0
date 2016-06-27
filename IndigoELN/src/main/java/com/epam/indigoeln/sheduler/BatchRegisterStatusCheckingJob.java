package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.mongodb.BasicDBObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class BatchRegisterStatusCheckingJob {

    public static final String BATCH_REGISTRATION_STATUS_FIELD = "registrationStatus";
    public static final String BATCH_REGISTRATION_JOB_ID_FIELD = "registrationJobId";
    public static final String BATCH_REGISTRATION_REPOSITORY_ID_FIELD = "registrationRepositoryId";
    public static final String BATCH_FULL_NBK_BATCH_FIELD = "fullNbkBatch";

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRegisterStatusCheckingJob.class);

    private static final long DELAY = 1000 * 60L; // 1 minute

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedDelay = DELAY)
    public void execute() {
        LOGGER.debug("Batch registration status checking job started");
        final List<Experiment> experiments = experimentRepository.findAll();
        final List<BasicDBObject> batchesOnRegistration = experiments.stream()
                .map(e -> new ExperimentDTO(e))
                .flatMap(e -> BatchComponentUtil.retrieveBatches(e.getComponents()).stream())
                .filter(b -> {
                    final String registrationStatus = b.getString(BATCH_REGISTRATION_STATUS_FIELD);
                    return RegistrationStatus.Status.IN_PROGRESS.toString().equals(registrationStatus);
                }).collect(Collectors.toList());
        Map<String, RegistrationStatus.Status> updatedBatchesStatuses = new HashMap<>();
        for (BasicDBObject batch : batchesOnRegistration) {
            final String repositoryId = batch.getString(BATCH_REGISTRATION_REPOSITORY_ID_FIELD);
            final long jobId = batch.getLong(BATCH_REGISTRATION_JOB_ID_FIELD);
            try {
                final RegistrationStatus status = registrationService.getStatus(repositoryId, jobId);
                if (!status.getStatus().equals(RegistrationStatus.Status.IN_PROGRESS)) {
                    updatedBatchesStatuses.put(batch.getString(BATCH_FULL_NBK_BATCH_FIELD), status.getStatus());
                }
            } catch (RegistrationException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error occurred while checking registration status, job id: " + jobId + ", repository id: " + repositoryId, e);
                }
            }
        }

        if (!MapUtils.isEmpty(updatedBatchesStatuses)) {
            template.convertAndSend("/topic/registration_status", updatedBatchesStatuses);
        }
    }

}
