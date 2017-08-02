package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationJobRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.mongodb.BasicDBObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BatchRegisterStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRegisterStatusCheckingJob.class);

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private RegistrationJobRepository registrationJobRepository;
    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRateString = "${indigoeln.schedule.batch.register.status.check.rate:10}000")
    public void execute() {
        Map<String, RegistrationStatus> updatedBatchesStatuses = new HashMap<>();

        RegistrationJob registrationJob = registrationJobRepository.findOneForCheck();

        if (registrationJob != null) {
            String jobId = registrationJob.getRegistrationJobId();
            String repositoryId = registrationJob.getRegistrationRepositoryId();

            if (!StringUtils.isAnyBlank(jobId, repositoryId)) {
                try {
                    RegistrationStatus status = registrationService.getStatus(repositoryId, jobId);

                    if (status.getStatus() != RegistrationStatus.Status.IN_PROGRESS) {
                        List<BasicDBObject> batchesOnRegistration = experimentRepository.findAll()
                                .stream()
                                .map(ExperimentDTO::new)
                                .flatMap(e -> BatchComponentUtil.retrieveBatches(e.getComponents()).stream())
                                .filter(b -> jobId.equals(b.get("registrationJobId")) && repositoryId.equals(b.get("registrationRepositoryId")))
                                .collect(Collectors.toList());

                        batchesOnRegistration.forEach(b -> updatedBatchesStatuses.put(String.valueOf(b.get("fullNbkBatch")), status));
                    }

                    registrationJob.setRegistrationStatus(status.getStatus());

                    try {
                        registrationJob.setLastHandledBy(InetAddress.getLocalHost().getCanonicalHostName());
                    } catch (Exception e) {
                        registrationJob.setLastHandledBy("Unknown");
                        LOGGER.trace("Error getting host name", e);
                    }

                    registrationJobRepository.save(registrationJob);
                } catch (RegistrationException e) {
                    LOGGER.error("Error occurred while checking registration status, job id: " + jobId + ", repository id: " + repositoryId, e);
                }
            }
        }

        if (!MapUtils.isEmpty(updatedBatchesStatuses)) {
            template.convertAndSend("/topic/registration_status", updatedBatchesStatuses);
        }
    }
}
