package com.epam.indigoeln.core.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.springframework.util.StringUtils;

import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for Batches
 */
public final class BatchComponentUtil {

    public static final String COMPONENT_NAME_BATCH_SUMMARY = "productBatchSummary";
    public static final String COMPONENT_NAME_REACTION_DETAILS = "reactionDetails";
    public static final String COMPONENT_NAME_CONCEPT_DETAILS = "conceptDetails";
    public static final String COMPONENT_FIELD_TITLE = "title";
    public static final String COMPONENT_FIELD_BATCHES = "batches";
    public static final String COMPONENT_FIELD_NBK_BATCH = "nbkBatch";
    public static final String COMPONENT_FIELD_FULL_NBK_BATCH = "fullNbkBatch";

    private static final String FORMAT_BATCH_NUMBER  = "000";
    private static final String PATTERN_BATCH_NUMBER = "\\d{3}";

    private BatchComponentUtil() {
    }

    public static Optional<ComponentDTO> getReactionDetails(Collection<ComponentDTO> components) {
        return getComponent(components, COMPONENT_NAME_REACTION_DETAILS);
    }

    public static Optional<ComponentDTO> getConceptDetails(Collection<ComponentDTO> components) {
        return getComponent(components, COMPONENT_NAME_CONCEPT_DETAILS);
    }

    public static Optional<ComponentDTO> getComponent(Collection<ComponentDTO> components, String componentName) {
        Predicate<ComponentDTO> batchFilter = c -> componentName.equals(c.getName()) &&
                c.getContent() != null;

        return components.stream().filter(batchFilter).findFirst();
    }

    public static String getComponentTitle(ComponentDTO component) {
        return (String) getComponentField(component, COMPONENT_FIELD_TITLE);
    }

    public static Object getComponentField(ComponentDTO component, String fieldName) {
        final BasicDBObject content = component.getContent();
        return content.get(fieldName);
    }

    /**
     * Retrieve json content of all batches for each component in received list
     * Filter components named as 'productBatchSummary' and return list of nested batches of each component
     *
     * @param components list of components
     * @return list of batches
     */
    @SuppressWarnings("unchecked")
    public static List<BasicDBObject> retrieveBatches(Collection<ComponentDTO> components) {
        Predicate<ComponentDTO> batchFilter = c -> COMPONENT_NAME_BATCH_SUMMARY.equals(c.getName()) &&
                c.getContent() != null && c.getContent().containsField(COMPONENT_FIELD_BATCHES);

        return components.stream().filter(batchFilter).
               map(component -> (BasicDBList) component.getContent().get(COMPONENT_FIELD_BATCHES)).
               flatMap(Collection::stream).
               map(o -> (BasicDBObject) o).
               collect(toList());
    }


    /**
     * Retrieve batch numbers list for all batches found in received components
     * Filter components named as 'productBatchSummary' and return list of nested batches of each component
     *
     * @param components list of components
     * @return list of batch numbers
     */
    public static List<String> retrieveBatchNumbers(Collection<ComponentDTO> components) {
        return  retrieveBatches(components).stream().
                filter(batch -> batch.containsField(COMPONENT_FIELD_NBK_BATCH)).
                map(batch -> batch.get(COMPONENT_FIELD_NBK_BATCH).toString()).collect(toList());
    }

    /**
     * Find last (maximal) existing batch number for all batches, that contains in components assigned with experiment
     * For proper comparison only batches with batch number specified in NUMERIC (000) format considered
     * If experiment does not contains any batches matches expected format empty result will be returned
     * Method could be used for batch number auto-generation in 'productBatchSummary' components
     *
     * @param experiment experiment
     * @return latest (maximal) existing batch number
     */
    public static OptionalInt getLastBatchNumber(ExperimentDTO experiment) {
        Pattern pattern = Pattern.compile(PATTERN_BATCH_NUMBER);
        Collection<ComponentDTO> components = Optional.ofNullable(experiment.getComponents()).orElse(Collections.emptyList());

        List<String> batchNumbers = retrieveBatchNumbers(components);
        return batchNumbers.stream().filter(item -> item != null && pattern.matcher(item).matches()).
                mapToInt(Integer::parseInt).max();
    }

    /**
     * Format numeric value as batch number string
     *
     * @param number numeric value
     * @return formatted batch number string
     */
    public static String formatBatchNumber(int number) {
        Format formatter = new DecimalFormat(FORMAT_BATCH_NUMBER);
        return formatter.format(number);
    }

    /**
     * Check, that received string corresponds to batch number numeric format
     *
     * @param batchNumber string to validate
     * @return is received string well-formatted batch number
     */
    public static boolean isValidBatchNumber(String batchNumber) {
        return !StringUtils.isEmpty(batchNumber) && Pattern.compile(PATTERN_BATCH_NUMBER).matcher(batchNumber).matches();
    }

}


