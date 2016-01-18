package com.epam.indigoeln.web.rest.util;

import org.springframework.http.HttpHeaders;

/**
 * Utility class for http header creation.
 */
public class HeaderUtil {

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-indigoeln-alert", message);
        headers.add("X-indigoeln-params", param);
        return headers;
    }

    public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-indigoeln-error", defaultMessage);
        headers.add("X-indigoeln-params", entityName);
        return headers;
    }
}
