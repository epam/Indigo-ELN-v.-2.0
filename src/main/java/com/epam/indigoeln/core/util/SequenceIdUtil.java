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
        return extractParentId(entity.getId());
    }

    public static String extractParentId(String id) {
        String[] split = id.split(DELIMITER);
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
