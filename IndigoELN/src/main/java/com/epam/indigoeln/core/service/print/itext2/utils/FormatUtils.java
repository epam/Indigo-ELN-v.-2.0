package com.epam.indigoeln.core.service.print.itext2.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

public class FormatUtils {
    private static final String DELIMITER = " ";
    private static final SimpleDateFormat ATTACHMENTS_FORMAT = new SimpleDateFormat("d MMMM yyyy");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "#.####");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
            .ofPattern("MMM d, yyyy HH:mm:ss z")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter BATCH_DETAILS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .withZone(ZoneId.systemDefault());
    private static final String UNITS = "kMGTPE";
    private static final String SIZE_FORMAT = "%.2f %sB";


    private FormatUtils() {
    }

    public static String formatSafe(TemporalAccessor temporalAccessor) {
        return Optional.ofNullable(temporalAccessor)
                .map(DEFAULT_FORMATTER::format)
                .orElse("");
    }

    public static String format(String date) {
        if (!StringUtils.isBlank(date)) {
            TemporalAccessor parse = BATCH_DETAILS_FORMATTER.parse(date);
            return formatSafe(parse);
        }
        return StringUtils.EMPTY;
    }

    public static String formatDecimal(String number) {
        if (StringUtils.isBlank(number)) {
            return StringUtils.EMPTY;
        }
        double value = Double.parseDouble(number);
        String result = DECIMAL_FORMAT.format(value);
        if (!StringUtils.isBlank(result) && !"0".equals(result)) {
            return result;
        }
        return StringUtils.EMPTY;
    }

    public static String formatDecimal(String number, String unit) {
        String formattedNumber = formatDecimal(number);
        if (!StringUtils.isAnyBlank(formattedNumber, unit)) {
            return formattedNumber + DELIMITER + unit;
        }else {
            return formattedNumber;
        }
    }

    public static String format(long size){
        int base = 1000;
        if (size < base) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(base));
        char unit = UNITS.charAt(exp-1);
        return String.format(SIZE_FORMAT, size / Math.pow(base, exp), unit);
    }

    public static String format(Date date){
        if (date != null){
            return ATTACHMENTS_FORMAT.format(date);
        }else {
            return StringUtils.EMPTY;
        }
    }
}
