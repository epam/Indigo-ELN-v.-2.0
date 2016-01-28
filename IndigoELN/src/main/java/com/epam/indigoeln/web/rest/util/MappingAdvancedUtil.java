package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Authority;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.json.JSONObject;

import java.util.Set;
import java.util.stream.Collectors;

public final class MappingAdvancedUtil {

    private MappingAdvancedUtil() {
    }

    public static BasicDBObject convertJsonToDbObject(JSONObject json) {
        return json != null ? (BasicDBObject) JSON.parse(json.toString()) : null;
    }

    public static Set<Authority> convertAuthorities(Set<String> authorities) {
        return authorities.stream().map(Authority::new).collect(Collectors.toSet());
    }
}
