package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

public final class SequenceIdUtil {

    public static final String EXPERIMENT_NAME_FORMAT = "0000";
    public static final String DELIMITER = "-";

    private SequenceIdUtil() {
    }

    public static String extractShortId(BasicModelObject entity) {
        if (entity.getId() == null) {
            return null;
        }
        String[] split = entity.getId().split(DELIMITER);
        return split[split.length - 1];
    }

    public static String extractParentId(BasicModelObject entity) {
        if (entity.getId() == null) {
            return null;
        }
        String[] split = entity.getId().split(DELIMITER);
        return split.length > 1 ? split[split.length - 2] : null;
    }

    public static String buildFullId(String ... ids) {
        return StringUtils.arrayToDelimitedString(ids, DELIMITER);
    }

    public static String generateExperimentName(Experiment experiment) {
        String shortId = extractShortId(experiment);
        return shortId != null ? new DecimalFormat(EXPERIMENT_NAME_FORMAT).format(Long.valueOf(shortId)) : null;
    }

    public static String extractFirstId(BasicDTO notebook){
        if (notebook.getFullId() == null){
            return null;
        }
        String[] split = notebook.getFullId().split(DELIMITER);
        return split.length > 0 ? split[0] : null;
    }

}
