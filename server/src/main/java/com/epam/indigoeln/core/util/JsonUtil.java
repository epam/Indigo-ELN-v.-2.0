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
package com.epam.indigoeln.core.util;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for Json Conversion.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Returns BasicDbList object from json array.
     *
     * @param jsonArray Json array
     * @return Returns BasicDbList
     */
    public static List<Object> basicDBListFromJsonArray(JSONArray jsonArray) {
        List<Object> result = new ArrayList<>();
        result.addAll(Arrays.asList(jsonToArray(jsonArray)));
        return result;
    }

    /**
     * Convert json array to object array.
     *
     * @param array Json array
     * @return Array of objects
     */
    public static Object[] jsonToArray(JSONArray array) {
        try {
            return new ObjectMapper().readValue(array.toString(), List.class).toArray();
        } catch (IOException e) {
            throw new CustomParametrizedException(e);
        }
    }
}
