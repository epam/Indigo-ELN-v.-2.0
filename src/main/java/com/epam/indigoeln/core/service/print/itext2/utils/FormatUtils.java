package com.epam.indigoeln.core.service.print.itext2.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

/**
 * Provides functionality for working with information's format.
 */
public final class FormatUtils {
    private static final String DELIMITER = " ";
    private static final SimpleDateFormat ATTACHMENTS_FORMAT = new SimpleDateFormat("d MMMM yyyy");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
            .ofPattern("MMM d, yyyy HH:mm:ss z")
            .withZone(ZoneId.systemDefault());
    private static final String UNITS = "kMGTPE";
    private static final String SIZE_FORMAT = "%.2f %sB";


    private FormatUtils() {
    }

    /**
     * Returns input object in the default string format.
     *
     * @param temporalAccessor Object which defines access to a temporal object,
     *                         such as a date, time and etc.
     * @return Returns string in the default format
     * @see java.time.temporal.TemporalAccessor
     */
    public static String formatSafe(TemporalAccessor temporalAccessor) {
        return Optional.ofNullable(temporalAccessor)
                .map(DEFAULT_FORMATTER::format)
                .orElse("");
    }

    /**
     * Returns date in the string format.
     *
     * @param date Date
     * @return Returns date in the string format
     */
    public static String formatBatchDetails(Date date) {
        if (date != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return formatSafe(zonedDateTime);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns string formatted number.
     *
     * @param number Number
     * @return Returns string formatted number
     */
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

    /**
     * Returns string formatted number.
     *
     * @param number Number
     * @param unit   Unit
     * @return Returns string formatted number
     */
    public static String formatDecimal(String number, String unit) {
        String formattedNumber = formatDecimal(number);
        if (!StringUtils.isAnyBlank(formattedNumber, unit)) {
            return formattedNumber + DELIMITER + unit;
        } else {
            return formattedNumber;
        }
    }

    /**
     * Returns formatted size.
     *
     * @param size Size
     * @return Returns formatted size
     */
    public static String format(long size) {
        int base = 1000;
        if (size < base) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(base));
        char unit = UNITS.charAt(exp - 1);
        return String.format(SIZE_FORMAT, size / Math.pow(base, exp), unit);
    }

    public static String format(Date date) {
        if (date != null) {
            return ATTACHMENTS_FORMAT.format(date);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
