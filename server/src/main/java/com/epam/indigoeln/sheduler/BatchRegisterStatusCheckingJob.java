/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationJobRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
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
                        List<Document> batchesOnRegistration = experimentRepository.findAll()
                                .stream()
                                .map(ExperimentDTO::new)
                                .flatMap(e -> BatchComponentUtil.retrieveBatches(e.getComponents()).stream())
                                .filter(b -> jobId.equals(b.get("registrationJobId"))
                                        && repositoryId.equals(b.get("registrationRepositoryId")))
                                .collect(Collectors.toList());

                        batchesOnRegistration.forEach(b -> updatedBatchesStatuses
                                .put(String.valueOf(b.get("fullNbkBatch")), status));
                    }

                    registrationJob.setRegistrationStatus(status.getStatus());
                    registrationJobRepository.save(registrationJob);
                } catch (RegistrationException e) {
                    LOGGER.error("Error occurred while checking registration status, job id: "
                            + jobId + ", repository id: " + repositoryId, e);
                }
            }
        }

        if (!MapUtils.isEmpty(updatedBatchesStatuses)) {
            template.convertAndSend("/topic/registration_status", updatedBatchesStatuses);
        }
    }
}
