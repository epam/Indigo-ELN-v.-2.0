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

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import org.bson.Document;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for Batches.
 */
public final class BatchComponentUtil {

    private static final String COMPONENT_FIELD_TITLE = "title";
    public static final String COMPONENT_FIELD_BATCHES = "batches";
    public static final String COMPONENT_FIELD_NBK_BATCH = "nbkBatch";
    private static final String COMPONENT_FIELD_FULL_NBK_BATCH = "fullNbkBatch";
    public static final String COMPONENT_FIELD_REGISTRATION_STATUS = "registrationStatus";

    public static final String REACTION_DETAILS = "reactionDetails";
    public static final String CONCEPT_DETAILS = "conceptDetails";
    public static final String REACTION = "reaction";
    public static final String PREFERRED_COMPOUND_SUMMARY = "preferredCompoundSummary";
    public static final String STOICH_TABLE = "stoichTable";
    public static final String EXPERIMENT_DESCRIPTION = "experimentDescription";
    public static final String PRODUCT_BATCH_SUMMARY = "productBatchSummary";
    public static final String PRODUCT_BATCH_DETAILS = "productBatchDetails";
    public static final String ATTACHMENTS = "attachments";

    private static final String FORMAT_BATCH_NUMBER = "000";
    private static final String PATTERN_BATCH_NUMBER = "\\d{3}";

    private BatchComponentUtil() {
    }

    /**
     * Returns reaction details.
     *
     * @param components Collection of component.
     * @return Reaction details.
     */
    public static Optional<ComponentDTO> getReactionDetails(Collection<ComponentDTO> components) {
        return getComponent(components, REACTION_DETAILS);
    }

    /**
     * Returns concept details.
     *
     * @param components Components
     * @return Concept details
     */
    public static Optional<ComponentDTO> getConceptDetails(Collection<ComponentDTO> components) {
        return getComponent(components, CONCEPT_DETAILS);
    }

    private static Optional<ComponentDTO> getComponent(Collection<ComponentDTO> components, String componentName) {
        Predicate<ComponentDTO> batchFilter = c -> componentName.equals(c.getName())
                && c.getContent() != null;

        return components.stream().filter(batchFilter).findFirst();
    }

    /**
     * Returns component title.
     *
     * @param component Component
     * @return Component title
     */
    public static String getComponentTitle(ComponentDTO component) {
        return (String) getComponentField(component, COMPONENT_FIELD_TITLE);
    }

    private static Object getComponentField(ComponentDTO component, String fieldName) {
        final Document content = component.getContent();
        return content.get(fieldName);
    }

    /**
     * Retrieves json content of all batches for each component in received list.
     * Filter components named as 'productBatchSummary' and return list of nested batches of each component.
     *
     * @param components list of components.
     * @return list of batches
     */
    @SuppressWarnings("unchecked")
    public static List<Document> retrieveBatches(Collection<ComponentDTO> components) {
        Predicate<ComponentDTO> batchFilter = c -> PRODUCT_BATCH_SUMMARY.equals(c.getName())
                && c.getContent() != null && c.getContent().containsKey(COMPONENT_FIELD_BATCHES);

        return components.stream().filter(batchFilter).
                map(component -> (List<Document>) component.getContent().get(COMPONENT_FIELD_BATCHES)).
                flatMap(Collection::stream).
                collect(toList());
    }

    /**
     * Retrieves batches from client.
     * Filter components named as 'productBatchSummary' and return list of nested batches of each component.
     *
     * @param components Collection of components
     * @return List of components.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> retrieveBatchesFromClient(Collection<Component> components) {
        Predicate<ComponentDTO> batchFilter = c -> PRODUCT_BATCH_SUMMARY.equals(c.getName())
                && c.getContent() != null && c.getContent().containsKey(COMPONENT_FIELD_BATCHES);

        return components.stream()
                .map(ComponentDTO::new)
                .filter(batchFilter)
                .flatMap(component -> ((List<Map<String, Object>>) component.getContent().get(COMPONENT_FIELD_BATCHES)).stream())
                .collect(toList());
    }

    /**
     * Retrieve batch numbers list for all batches found in received components.
     * Filter components named as 'productBatchSummary' and return list of nested batches of each component.
     *
     * @param components list of components
     * @return list of batch numbers
     */
    private static List<String> retrieveBatchNumbers(Collection<ComponentDTO> components) {
        return retrieveBatches(components).stream().
                filter(batch -> batch.containsKey(COMPONENT_FIELD_NBK_BATCH)).
                map(batch -> batch.get(COMPONENT_FIELD_NBK_BATCH).toString()).collect(toList());
    }

    /**
     * Find last (maximal) existing batch number for all batches, that contains in components assigned with experiment.
     * For proper comparison only batches with batch number specified in NUMERIC (000) format considered.
     * If experiment does not contains any batches matches expected format empty result will be returned.
     * Method could be used for batch number auto-generation in 'productBatchSummary' components.
     *
     * @param experiment experiment
     * @return latest (maximal) existing batch number
     */
    public static OptionalInt getLastBatchNumber(ExperimentDTO experiment) {
        Pattern pattern = Pattern.compile(PATTERN_BATCH_NUMBER);
        Collection<ComponentDTO> components = Optional.ofNullable(experiment.getComponents())
                .orElse(Collections.emptyList());

        List<String> batchNumbers = retrieveBatchNumbers(components);
        return batchNumbers.stream().filter(item -> item != null && pattern.matcher(item).matches()).
                mapToInt(Integer::parseInt).max();
    }

    /**
     * Format numeric value as batch number string.
     *
     * @param number numeric value
     * @return formatted batch number string
     */
    public static String formatBatchNumber(int number) {
        Format formatter = new DecimalFormat(FORMAT_BATCH_NUMBER);
        return formatter.format(number);
    }

    /**
     * Check, that received string corresponds to batch number numeric format.
     *
     * @param batchNumber string to validate
     * @return is received string well-formatted batch number
     */
    public static boolean isValidBatchNumber(String batchNumber) {
        return !StringUtils.isEmpty(batchNumber) && Pattern.compile(PATTERN_BATCH_NUMBER)
                .matcher(batchNumber).matches();
    }

    /**
     * Returns batches for notebook.
     *
     * @param notebook Notebook
     * @return Batches
     */
    public static List<String> hasBatches(Notebook notebook) {
        List<ComponentDTO> components = notebook.getExperiments().stream()
                .flatMap(e -> e.getComponents().stream().map(ComponentDTO::new))
                .collect(Collectors.toList());
        return retrieveFullBatchNumbers(components);
    }

    private static List<String> retrieveFullBatchNumbers(Collection<ComponentDTO> components) {
        return retrieveBatches(components).stream().filter(b -> b.containsKey(COMPONENT_FIELD_FULL_NBK_BATCH))
                .map(b -> b.get(COMPONENT_FIELD_FULL_NBK_BATCH).toString()).collect(toList());
    }
}
