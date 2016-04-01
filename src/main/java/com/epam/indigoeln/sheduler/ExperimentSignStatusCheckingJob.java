package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

@Component
public class ExperimentSignStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSignStatusCheckingJob.class);

    private static final long DELAY = 1000 * 60L; // 1 minute

    @Autowired
    private ExperimentService experimentService;

    @Scheduled(fixedDelay = DELAY)
    public void execute() {
        LOGGER.debug("Experiment sign status checking job started");
        final Collection<ExperimentDTO> experiments = experimentService.getExperimentsByStatuses(Arrays.asList(ExperimentStatus.SUBMITTED,
                ExperimentStatus.SINGING));
        experiments.forEach(e -> {
            try {
                experimentService.checkExperimentStatus(e);
            } catch (Exception ex) {
                LOGGER.error("Error executing experiment sign status checking job!", ex);
            }
        });
    }

}
