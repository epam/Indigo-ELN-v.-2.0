package com.epam.indigoeln.web.rest.util;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Utility class for Json Conversion
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static Map jsonToMap(JSONObject jsonObject) {
        try {
            return new ObjectMapper().readValue(jsonObject.toString(), Map.class);
        } catch (IOException e) {
            throw new CustomParametrizedException(e.getMessage());
        }
    }

    public static JSONObject mapToJson(Map map) {
        return new JSONObject(map);
    }
}
