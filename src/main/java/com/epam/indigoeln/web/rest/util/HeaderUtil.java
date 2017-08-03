package com.epam.indigoeln.web.rest.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utility class for http header creation.
 */
public class HeaderUtil {

    private static final String SUCCESS_ALERT = "X-indigoeln-success-alert";

    private HeaderUtil() {
    }

    private static HttpHeaders createSuccessAlert(String message) {
        return createAlert(SUCCESS_ALERT, message);
    }

    private static HttpHeaders createAlert(String type, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(type, message);
        return headers;
    }

    public static HttpHeaders createAttachmentDescription(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        return headers;
    }

    public static HttpHeaders createEntityCreateAlert(String entityName, String param) {
        String message;
        if (param != null) {
            message = "The " + entityName + " \"" + param + "\"" + " is created";
        } else {
            message = "The " + entityName + " is successfully created";
        }
        return createSuccessAlert(message);
    }

    public static HttpHeaders createEntityDeleteAlert(String entityName, String param) {
        String message;
        if (param != null) {
            message = "The " + entityName + " \"" + param + "\"" + " is deleted";
        } else {
            message = "The " + entityName + " is successfully deleted";
        }
        return createSuccessAlert(message);
    }

    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        String message;
        if (param != null) {
            message = "The " + entityName + " \"" + param + "\"" + " is updated";
        } else {
            message = "The " + entityName + " is successfully updated";
        }
        return createSuccessAlert(message);
    }

    public static HttpHeaders createPdfPreviewHeaders(String fileName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.add("Content-disposition", "inline; filename=" + fileName);
        httpHeaders.add("Access-Control-Allow-Headers", "Range");
        httpHeaders.add("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
        return httpHeaders;
    }
}
