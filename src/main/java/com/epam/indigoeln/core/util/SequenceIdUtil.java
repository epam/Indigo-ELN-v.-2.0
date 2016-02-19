package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

public final class SequenceIdUtil {

    private static final String EXPERIMENT_NAME_FORMAT = "0000";
    private static final String BATCH_NUMBER_FORMAT = "000";
    private static final String DELIMITER = "-";

    private SequenceIdUtil() {
    }

    public static String extractShortId(BasicModelObject entity) {
        if(entity.getId() == null) return null;
        String[] split = entity.getId().split(DELIMITER);
        return split[split.length - 1];
    }

    public static String buildFullId(String ... ids) {
        return StringUtils.arrayToDelimitedString(ids, DELIMITER);
    }

    public static String generateExperimentName(Experiment experiment) {
        String shortId = extractShortId(experiment);
        return shortId != null ? new DecimalFormat(EXPERIMENT_NAME_FORMAT).format(Long.valueOf(shortId)) : null;
    }

    public static String generateExperimentTitle(Experiment experiment, Notebook notebook) {
        return notebook.getName() +  DELIMITER + generateExperimentName(experiment);
    }

    public static String formatBatchNumber(Long batchNumberLong) {
        return new DecimalFormat(BATCH_NUMBER_FORMAT).format(batchNumberLong);
    }

}
