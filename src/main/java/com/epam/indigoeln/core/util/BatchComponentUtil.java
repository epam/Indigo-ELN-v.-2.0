package com.epam.indigoeln.core.util;

import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.bson.BasicBSONObject;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for Batches
 */
public final class BatchComponentUtil {

    public static final String COMPONENT_NAME_BATCH_SUMMARY = "productBatchSummary";
    public static final String COMPONENT_FIELD_BATCHES = "batches";
    public static final String COMPONENT_FIELD_NBK_BATCH = "nbkBatch";

    private static final String FORMAT_BATCH_NUMBER  = "000";
    private static final String PATTERN_BATCH_NUMBER = "\\d{3}";


    private BatchComponentUtil() {
    }

    @SuppressWarnings("unchecked")
    public static List<BasicDBObject> retrieveBatches(Collection<ComponentDTO> components) {
        Predicate<ComponentDTO> batchFilter = c -> COMPONENT_NAME_BATCH_SUMMARY.equals(c.getName()) &&
                c.getContent() != null && c.getContent().containsField(COMPONENT_FIELD_BATCHES);

        return components.stream().filter(batchFilter).
               map(component -> (BasicDBList) component.getContent().get(COMPONENT_FIELD_BATCHES)).
               flatMap(Collection::stream).
               map(o -> (BasicDBObject) o).
               collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static Optional<Map> retrieveBatchByNumber(Collection<ComponentDTO> components, String batchNumber) {
         return retrieveBatches(components).stream().
                filter(batch -> batchNumber.equals(batch.get(COMPONENT_FIELD_NBK_BATCH).toString())).findAny().
                map(BasicBSONObject::toMap);
    }

    public static List<String> retrieveBatchNumbers(Collection<ComponentDTO> components) {
        return  retrieveBatches(components).stream().
                filter(batch -> batch.containsField(COMPONENT_FIELD_NBK_BATCH)).
                map(batch -> batch.get(COMPONENT_FIELD_NBK_BATCH).toString()).collect(Collectors.toList());
    }

    public static OptionalInt getLatestBatchNumber(ExperimentDTO experiment) {
        Pattern pattern = Pattern.compile(PATTERN_BATCH_NUMBER);
        Collection<ComponentDTO> components = Optional.ofNullable(experiment.getComponents()).orElse(Collections.emptyList());

        List<String> batchNumbers = retrieveBatchNumbers(components);
        return batchNumbers.stream().filter(item -> item != null && pattern.matcher(item).matches()).
                mapToInt(Integer::parseInt).max();
    }

    public static String formatBatchNumber(int number) {
        Format formatter = new DecimalFormat(FORMAT_BATCH_NUMBER);
        return formatter.format(number);
    }

    public static boolean isValidBatchNumber(String batchNumber) {
        return !StringUtils.isEmpty(batchNumber) && Pattern.compile(PATTERN_BATCH_NUMBER).matcher(batchNumber).matches();
    }

}


