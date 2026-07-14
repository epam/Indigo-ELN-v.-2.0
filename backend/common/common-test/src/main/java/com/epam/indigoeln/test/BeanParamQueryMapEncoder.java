package com.epam.indigoeln.test;

import feign.QueryMapEncoder;
import jakarta.ws.rs.QueryParam;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanParamQueryMapEncoder implements QueryMapEncoder {

    @Override
    public Map<String, Object> encode(Object object) {
        Map<String, Object> queryMap = new LinkedHashMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            QueryParam queryParam = field.getAnnotation(QueryParam.class);
            if (queryParam == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value != null) {
                    queryMap.put(queryParam.value(), value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return queryMap;
    }
}
