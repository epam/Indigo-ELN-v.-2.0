package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.model.SignatureJob;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.signature.SignatureJobRepository;
import com.epam.indigoeln.core.service.signature.SignatureService;
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
import java.util.Map;
import java.util.Objects;

@Component
public class ExperimentSignStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSignStatusCheckingJob.class);

    @Autowired
    private SignatureJobRepository signatureJobRepository;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRateString = "${indigoeln.schedule.experiment.sign.status.check.rate:10}000")
    public void execute() {
        Map<String, ExperimentStatus> updatedExperimentStatuses = new HashMap<>();

        SignatureJob signatureJob = signatureJobRepository.findOneForCheck();

        if (signatureJob != null && !StringUtils.isBlank(signatureJob.getExperimentId())) {
            Experiment experiment = experimentRepository.findOne(signatureJob.getExperimentId());

            if (experiment != null) {
                try {
                    ExperimentStatus origStatus = experiment.getStatus();
                    ExperimentStatus newStatus = signatureService.checkExperimentStatus(experiment);

                    if (!Objects.equals(origStatus, newStatus)) {
                        updatedExperimentStatuses.put(experiment.getId(), newStatus);
                    }

                    ExperimentStatus finalStatus = signatureService.getExperimentStatus(SignatureService.ISSStatus.valueOf(signatureService.getFinalStatus()));

                    if (Objects.equals(newStatus, finalStatus) || Objects.equals(newStatus, ExperimentStatus.SUBMIT_FAIL)) {
                        signatureJob.setExperimentStatus(newStatus);
                    } else {
                        signatureJob.setExperimentStatus(ExperimentStatus.SUBMITTED);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error executing experiment sign status checking job! Experiment = " + signatureJob.getExperimentId(), e);
                }
            }

            setLastHandledBy(signatureJob);

            signatureJobRepository.save(signatureJob);
        }

        if (!MapUtils.isEmpty(updatedExperimentStatuses)) {
            template.convertAndSend("/topic/experiment_status", updatedExperimentStatuses);
        }
    }

    private void setLastHandledBy(SignatureJob signatureJob) {
        try {
            signatureJob.setLastHandledBy(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (Exception e) {
            signatureJob.setLastHandledBy("Unknown");
            LOGGER.trace("Error getting host name", e);
        }
    }
}
