package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BatchRegisterStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRegisterStatusCheckingJob.class);

    private static final String BATCH_REGISTRATION_STATUS_FIELD = "registrationStatus";
    private static final String BATCH_REGISTRATION_JOB_ID_FIELD = "registrationJobId";
    private static final String BATCH_REGISTRATION_REPOSITORY_ID_FIELD = "registrationRepositoryId";
    private static final String BATCH_FULL_NBK_BATCH_FIELD = "fullNbkBatch";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRateString = "${indigoeln.schedule.batch.register.status.check.rate:60}000")
    public void execute() {
        LOGGER.debug("Batch registration status checking job started");

        List<BasicDBObject> batchesOnRegistration = experimentRepository.findAll()
                .stream()
                .map(ExperimentDTO::new)
                .flatMap(e -> BatchComponentUtil.retrieveBatches(e.getComponents()).stream())
                .filter(b -> {
                    String registrationStatus = b.getString(BATCH_REGISTRATION_STATUS_FIELD);
                    Object jobId = b.get(BATCH_REGISTRATION_JOB_ID_FIELD);
                    return RegistrationStatus.Status.IN_PROGRESS.toString().equals(registrationStatus) && jobId != null;
                })
                .collect(Collectors.toList());

        Map<String, RegistrationStatus> updatedBatchesStatuses = new HashMap<>();

        for (BasicDBObject batch : batchesOnRegistration) {
            String repositoryId = batch.getString(BATCH_REGISTRATION_REPOSITORY_ID_FIELD);

            long jobId = batch.getLong(BATCH_REGISTRATION_JOB_ID_FIELD);

            try {
                RegistrationStatus status = registrationService.getStatus(repositoryId, jobId);

                if (status.getStatus() != RegistrationStatus.Status.IN_PROGRESS) {
                    updatedBatchesStatuses.put(batch.getString(BATCH_FULL_NBK_BATCH_FIELD), status);
                }
            } catch (RegistrationException e) {
                LOGGER.error("Error occurred while checking registration status, job id: " + jobId + ", repository id: " + repositoryId, e);
            }
        }

        if (!MapUtils.isEmpty(updatedBatchesStatuses)) {
            template.convertAndSend("/topic/registration_status", updatedBatchesStatuses);
        }
    }
}
