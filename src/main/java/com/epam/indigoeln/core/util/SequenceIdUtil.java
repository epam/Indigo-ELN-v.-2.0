package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

/**
 * Utility class that provides for extraction and generating id.
 */
public final class SequenceIdUtil {

    private static final String EXPERIMENT_NAME_FORMAT = "0000";
    private static final String DELIMITER = "-";

    private SequenceIdUtil() {
    }

    /**
     * Extracts short id from entity.
     *
     * @param entity Entity
     * @return Id
     */
    public static String extractShortId(BasicModelObject entity) {
        if (entity.getId() == null) {
            return null;
        }
        return extractShortId(entity.getId());
    }

    /**
     * Extracts short part from given id
     * @param id id to extract short part
     * @return short part from given id
     */
    public static String extractShortId(String id) {
        String[] split = id.split(DELIMITER);
        return split[split.length - 1];
    }

    /**
     * Extracts parent id from entity.
     *
     * @param entity Entity
     * @return Parent id
     */
    public static String extractParentId(BasicModelObject entity) {
        if (entity.getId() == null) {
            return null;
        }
        String[] split = entity.getId().split(DELIMITER);
        return split.length > 1 ? split[split.length - 2] : null;
    }

    /**
     * Extracts first id from entity
     *
     * @param entity Entity
     * @return project's id
     */
    public static String extractFirstId(BasicModelObject entity) {
        if (entity.getId() == null) {
            return null;
        }
        String[] split = entity.getId().split(DELIMITER);
        return split.length > 0 ? split[0] : null;
    }

    /**
     * Builds full id.
     *
     * @param ids Ids for building full id
     * @return Full id
     */
    public static String buildFullId(String... ids) {
        return StringUtils.arrayToDelimitedString(ids, DELIMITER);
    }

    /**
     * Generates experiment's name.
     *
     * @param experiment Experiment
     * @return Experiment's name
     */
    public static String generateExperimentName(Experiment experiment) {
        String shortId = extractShortId(experiment);
        return shortId != null ? new DecimalFormat(EXPERIMENT_NAME_FORMAT).format(Long.valueOf(shortId)) : null;
    }

    /**
     * Extracts first id from notebook.
     *
     * @param notebook Notebook
     * @return Id
     */
    public static String extractFirstId(BasicDTO notebook) {
        if (notebook.getFullId() == null) {
            return null;
        }
        String[] split = notebook.getFullId().split(DELIMITER);
        return split.length > 0 ? split[0] : null;
    }
}
