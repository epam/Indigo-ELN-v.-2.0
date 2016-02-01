package com.epam.indigoeln.core.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public final class SequenceNumberGenerationUtil {

    public static final String FORMAT_EXPERIMENT_NUMBER = "0000";
    public static final String FORMAT_COMPONENT_NUMBER = "000";
    public static final String PATTERN_NUMERIC = "[0-9]+";

    private SequenceNumberGenerationUtil() {
    }

    /**
     * Generate next numeric  number in defined format
     * Filter existing Values by values, matches numeric format (such as "001", "002", "1234" etc ),
     * get maximum long value of filtered values, increment it and save in output string format
     * Component or Experiment numbers could be changed by user manually. So, numeric format is voluntary.
     * If no any numbers matches to the numeric format found, default "001" value will be returned
     * If received list of components is empty, default "001" value will be returned
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

        long nextComponentNumber = maxNumber.isPresent() ? maxNumber.getAsLong() + 1 : 1L;
        return formatter.format(nextComponentNumber);
    }

    public static String generateNextComponentNumber(Collection<String>  componentNumbers) {
        return generateNextNumber(componentNumbers, FORMAT_COMPONENT_NUMBER);
    }

    public static String generateNextExperimentNumber(Collection<String> experimentNumbers) {
        return generateNextNumber(experimentNumbers, FORMAT_EXPERIMENT_NUMBER);
    }

}
