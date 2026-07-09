package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import one.util.streamex.StreamEx;

import java.util.Arrays;

import static com.epam.indigoeln.eln.model.ExperimentStatus.*;

public class ExperimentWorkflowHelper {

    public static void ensureStatus(ExperimentEntity experiment, ExperimentStatus... allowedStatuses) {
        if (!Arrays.asList(allowedStatuses).contains(experiment.getStatus())) {
            InvalidRequestException.fail("Experiment is " + experiment.getStatus() + ", must be " + StreamEx.of(allowedStatuses).joining(" or "));
        }
    }

    public static void updateStatusFromSignature(ExperimentEntity experiment, DocumentStatus documentStatus) {
        switch (documentStatus) {
            case SIGNING -> {
                ensureStatus(experiment, SUBMITTED, SIGNING);
                experiment.setStatus(SIGNING);
            }
            case SIGNED -> {
                ensureStatus(experiment, SUBMITTED, SIGNING);
                experiment.setStatus(SIGNED);
                experiment.setStatus(ARCHIVED);
            }
            case REJECTED -> {
                ensureStatus(experiment, SUBMITTED, SIGNING, REJECTED);
                experiment.setStatus(REJECTED);
            }
        }
    }
}
