package com.epam.indigoeln.core.util;

import org.bson.BsonValue;
import org.springframework.data.mongodb.util.BsonUtils;

public class BsonUtil {

    public static String bsonValue(Object value) {
        if (value instanceof BsonValue) {
            value = BsonUtils.toJavaType((BsonValue) value);
        }
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
