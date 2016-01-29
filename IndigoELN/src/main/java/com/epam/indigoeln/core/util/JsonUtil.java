package com.epam.indigoeln.core.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import org.json.JSONArray;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Utility class for Json Conversion
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static BasicDBList basicDBListFromArray(Object[] array) {
        BasicDBList result = new BasicDBList();
        result.addAll(Arrays.asList(array));
        return result;
    }

    public static JSONArray arrayToJson(Object[] array) {
        return new JSONArray(array);
    }

    public static Object[] jsonToArray(JSONArray array) {
        try {
            return new ObjectMapper().readValue(array.toString(), List.class).toArray();
        } catch (IOException e) {
            throw new CustomParametrizedException(e.getMessage());
        }
    }
}
