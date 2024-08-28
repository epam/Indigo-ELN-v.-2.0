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

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.SignatureJob;
import com.epam.indigoeln.core.repository.signature.SignatureJobRepository;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ExperimentSignStatusCheckingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSignStatusCheckingJob.class);

    @Autowired
    private SignatureJobRepository signatureJobRepository;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ExperimentService experimentService;

    @Scheduled(fixedRateString = "${indigoeln.schedule.experiment.sign.status.check.rate:10}000")
    public void execute() {

        SignatureJob signatureJob = signatureJobRepository.findOneForCheck();

        if (signatureJob != null && !StringUtils.isBlank(signatureJob.getExperimentId())) {
            Experiment experiment = experimentService.getExperiment(signatureJob.getExperimentId());

            if (experiment != null) {
                try {
                    ExperimentStatus newStatus = signatureService.updateAndGetExperimentStatus(experiment);

                    ExperimentStatus finalStatus = signatureService
                            .getExperimentStatus(SignatureService.ISSStatus.valueOf(signatureService.getFinalStatus()));

                    if (Objects.equals(newStatus, finalStatus)
                            || Objects.equals(newStatus, ExperimentStatus.SUBMIT_FAIL)) {
                        signatureJob.setExperimentStatus(newStatus);
                    } else {
                        signatureJob.setExperimentStatus(ExperimentStatus.SUBMITTED);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error executing experiment sign status checking job! Experiment = "
                            + signatureJob.getExperimentId(), e);
                }
            }
            signatureJobRepository.save(signatureJob);
        }
    }
}