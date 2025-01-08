/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature;

import com.chemistry.enotebook.signature.entity.JsonRepresentable;
import com.chemistry.enotebook.signature.security.SessionCache;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.UUID;

public class Util {

    private static SimpleDateFormat sm = new SimpleDateFormat("dd.MM.yyyy-HH.mm");

    public static JsonObject requestBodyToJsonObject(HttpServletRequest request) throws IOException {
        String json  = IOUtils.toString(request.getInputStream());
        return toJsonObject(json);
    }

    public static JsonObject toJsonObject(String jsonStr) {
        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonStr.getBytes()));
        return jsonReader.readObject();
    }

    public static String generateErrorJsonCodeString(Integer code, String text) {
        return Json.createObjectBuilder()
                .add("error", Json.createObjectBuilder()
                        .add("code", code)
                        .add("text", text)
                        .build())
                .build().toString();
    }

    public static String generateErrorJsonString(String text) {
        return Json.createObjectBuilder()
                .add("error", Json.createObjectBuilder()
                        .add("text", text)
                        .build())
                .build().toString();
    }

    public static JsonObject generateObjectContainingArray(String arrayName, Collection<? extends JsonRepresentable> items) {
        return Json.createObjectBuilder()
                .add(arrayName, generateJsonArray(items))
                .build();
    }

    public static JsonArray generateJsonArray(Collection<? extends JsonRepresentable> items) {
        JsonArrayBuilder usersJsonBuilder = Json.createArrayBuilder();
        if(items!=null) {
            for(JsonRepresentable item : items) {
                if(item != null) {
                    usersJsonBuilder.add(item.asJson());
                }
            }
        }
        return usersJsonBuilder.build();
    }

    public static String convertToStringDate(long timeMillis) {
        if(timeMillis != 0) {
            return sm.format(timeMillis);
        }
        return "";
    }
    public static java.sql.Timestamp convertToSqlDate(long timeMillis) {
        if(timeMillis != 0) {
            return new java.sql.Timestamp(timeMillis);
        }
        return null;
    }

    public static UUID uuidFromObject(Object uuidObj) {
        if(uuidObj != null) {
            if(uuidObj instanceof UUID) {
                return (UUID)uuidObj;
            } else if(uuidObj instanceof String) {
                return UUID.fromString((String)uuidObj);
            }
        }
        return UUID.randomUUID();
    }

    public static long dateLongFromObject(Object uuidObj) {
        if(uuidObj != null) {
            if(uuidObj instanceof java.sql.Date) {
                return ((java.sql.Date)uuidObj).getTime();
            } else if(uuidObj instanceof Timestamp) {
                return ((Timestamp) uuidObj).getTime();
            } else if(uuidObj instanceof java.util.Date) {
                return ((java.util.Date)uuidObj).getTime();
            } else if(uuidObj instanceof String) {
                return java.sql.Date.valueOf((String)uuidObj).getTime();
            }
        }
        return System.currentTimeMillis();
    }

    public static String getUsername(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public static String getMessageAboutDocumentRemove(String documentName) {
        return "The document " + documentName + " was changed after the last signature and will be removed from document list.";
    }
}
