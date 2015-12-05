package com.epam.indigo.bingodb.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    public static BingoResult handleError(Throwable e, String format, Object... args) {
        String errorMessage = String.format(format, args);
        LOGGER.error(errorMessage, e);
        return BingoResult.failure().withErrorMessage(errorMessage);
    }
}
