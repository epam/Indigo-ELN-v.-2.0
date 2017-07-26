package com.epam.indigoeln.core.service.print.itext2.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class DateTimeUtils {
    private DateTimeUtils() {
    }

    private static String defaultFormat = "MMM d, yyyy HH:mm:ss z"; // ex: Jul 20, 2017 15:34:21 MSK
    private static DateTimeFormatter defaultFormatter = DateTimeFormatter
            .ofPattern(defaultFormat)
            .withZone(ZoneId.systemDefault());

    public static String formatSafe(TemporalAccessor temporalAccessor) {
        return Optional.ofNullable(temporalAccessor)
                .map(defaultFormatter::format)
                .orElse("");
    }

}
