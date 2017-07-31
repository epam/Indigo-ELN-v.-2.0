package com.epam.indigoeln.core.service.print.itext2.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class FormatUtils {
    private static String defaultFormat = "MMM d, yyyy HH:mm:ss z"; // ex: Jul 20, 2017 15:34:21 MSK
    private static final String BATCH_DETAILS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String DECIMAL_FORMAT = "#.####";
    private static final String DELIMITER = " ";
    private static final DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
            .ofPattern(defaultFormat)
            .withZone(ZoneId.systemDefault());

    private FormatUtils() {
    }

    public static String formatSafe(TemporalAccessor temporalAccessor) {
        return Optional.ofNullable(temporalAccessor)
                .map(DEFAULT_FORMATTER::format)
                .orElse("");
    }

    public static String format(String date){
        if (!StringUtils.isBlank(date)){
            TemporalAccessor parse = DateTimeFormatter.ofPattern(BATCH_DETAILS_FORMAT)
                    .withZone(ZoneId.systemDefault())
                    .parse(date);
            return formatSafe(parse);
        }
       return StringUtils.EMPTY;
    }

    public static String formatDecimal(String number){
        double value = Double.parseDouble(number);
        String result = decimalFormat.format(value);
        if (!StringUtils.isBlank(result) && !"0".equals(result)){
            return result;
        }
        return StringUtils.EMPTY;
    }

    public static String formatDecimal(String number, String unit){
        double value = Double.parseDouble(number);
        String result = decimalFormat.format(value);
        if (!StringUtils.isBlank(result) && !"0".equals(result)){
            if (!StringUtils.isBlank(unit)){
                return result + DELIMITER + unit;
            }else {
                return result;
            }
        }
        return StringUtils.EMPTY;
    }

}
