package com.epam.indigoeln.core.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public final class SequenceNumberGenerationUtil {

    public static final String FORMAT_EXPERIMENT_NUMBER = "0000";
    public static final String FORMAT_BATCH_NUMBER = "000";
    public static final String PATTERN_NUMERIC = "[0-9]+";

    private SequenceNumberGenerationUtil() {
    }

    /**
     * Generate next numeric  number in defined format
     * Filter existing Values by values, matches numeric format (such as "001", "002", "1234" etc ),
     * get maximum long value of filtered values, increment it and save in output string format
     * Batch or Experiment numbers could be changed by user manually. So, numeric format is voluntary.
     * If no any numbers matches to the numeric format found, default "001" value will be returned
     * If received list of batches is empty, default "001" value will be returned
     *
     * @param existingValues list of existing values
     * @return next formatted numeric value
     */
    public static String generateNextNumber(Collection<String> existingValues, String numberFormat) {
        Pattern pattern = Pattern.compile(PATTERN_NUMERIC);
        Format formatter = new DecimalFormat(numberFormat);

        OptionalLong maxNumber = existingValues.stream().
                filter(item -> item != null && pattern.matcher(item).matches()).
                mapToLong(Long::parseLong).
                max();

        long nextBatchNumber = maxNumber.isPresent() ? maxNumber.getAsLong() + 1 : 1L;
        return formatter.format(nextBatchNumber);
    }

    public static String generateNextBatchNumber(Collection<String>  batchNumbers) {
        return generateNextNumber(batchNumbers, FORMAT_BATCH_NUMBER);
    }

    public static String generateNextExperimentNumber(Collection<String> experimentNumbers) {
        return generateNextNumber(experimentNumbers, FORMAT_EXPERIMENT_NUMBER);
    }

}
