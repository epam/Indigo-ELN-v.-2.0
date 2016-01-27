package com.epam.indigoeln.web.rest.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

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

    public static HttpHeaders createAttachmentDescription(String filename, String contentType, long contentLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(contentLength);
        try {
            headers.setContentType(MediaType.parseMediaType(contentType));
        } catch (InvalidMediaTypeException ex) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
        return createAlert("A new " + entityName + " is created with identifier " + param, param);
    }

    public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
        return createAlert("A " + entityName + " is deleted with identifier " + param, param);
    }

    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        return createAlert("A " + entityName + " is updated with identifier " + param, param);
    }
}
