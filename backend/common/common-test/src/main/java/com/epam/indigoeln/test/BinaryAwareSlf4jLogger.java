package com.epam.indigoeln.test;

import feign.Logger;
import feign.Request;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Collection;
import java.util.Map;

/**
 * Feign {@link Logger} that behaves exactly like the built-in {@code Slf4jLogger} with
 * {@link Level#FULL}, but encodes binary request bodies as Base64 instead of replacing
 * them with the unhelpful placeholder text {@code "Binary data"}.
 */
public class BinaryAwareSlf4jLogger extends Logger {

    private final org.slf4j.Logger log;

    public BinaryAwareSlf4jLogger(String name) {
        this.log = LoggerFactory.getLogger(name);
    }

    // -----------------------------------------------------------------------
    // Logger contract
    // -----------------------------------------------------------------------

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(methodTag(configKey) + format, args));
        }
    }

    /**
     * Same as the default {@link Logger#logRequest} implementation, except that a binary body is
     * Base64-encoded rather than replaced with {@code "Binary data"}.
     */
    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log(configKey, "---> %s %s HTTP/1.1", request.httpMethod().name(), request.url());

        if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
            for (Map.Entry<String, Collection<String>> field : request.headers().entrySet()) {
                for (String value : field.getValue()) {
                    log(configKey, "%s: %s", field.getKey(), value);
                }
            }

            if (logLevel.ordinal() >= Level.FULL.ordinal()) {
                byte[] body = request.body();
                if (body != null) {
                    String bodyText;
                    if (request.isBinary()) {
                        bodyText = "Binary (base64): " + Base64.getEncoder().encodeToString(body);
                    } else {
                        bodyText = request.charset() != null
                                ? new String(body, request.charset())
                                : new String(body);
                    }
                    log(configKey, "");
                    log(configKey, "%s", bodyText);
                    log(configKey, "---> END HTTP (%d-byte body)", body.length);
                } else {
                    log(configKey, "---> END HTTP (0-byte body)");
                }
            }
        }
    }
}

