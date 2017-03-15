package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.service.signature.SignatureService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExperimentSignStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSignStatusCheckingJob.class);

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRateString = "${indigoeln.schedule.experiment.sign.status.check.rate:60}000")
    public void execute() {
        LOGGER.debug("Experiment sign status checking job started");
        final List<Experiment> experiments = experimentRepository.findByStatusIn(Arrays.asList(ExperimentStatus.SUBMITTED,
                ExperimentStatus.SINGING));
        Map<String, ExperimentStatus> updatedExperimentsStatuses = new HashMap<>();
        experiments.forEach(experiment -> {
            try {
                final ExperimentStatus origStatus = experiment.getStatus();
                final ExperimentStatus newStatus = signatureService.checkExperimentStatus(experiment);
                if (!origStatus.equals(newStatus)) {
                    updatedExperimentsStatuses.put(experiment.getId(), newStatus);
                }
            } catch (Exception ex) {
                LOGGER.error("Error executing experiment sign status checking job!", ex);
            }
        });

        if (!MapUtils.isEmpty(updatedExperimentsStatuses)) {
            template.convertAndSend("/topic/experiment_status", updatedExperimentsStatuses);
        }
    }

}
