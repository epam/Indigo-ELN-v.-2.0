/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utility class for http header creation.
 */
public final class HeaderUtil {

    public static final String SUCCESS_ALERT = "X-indigoeln-success-alert";
    public static final String TOTAL_COUNT = "X-Total-Count";

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
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
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
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName);

        return httpHeaders;
    }
}
