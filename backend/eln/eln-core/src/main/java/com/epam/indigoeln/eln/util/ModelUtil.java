package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import one.util.streamex.StreamEx;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class ModelUtil {

    public static void updateDates(BaseEntity model, UserEntity currentUser) {
        ZonedDateTime date = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        if (model.getCreatedBy() == null) {
            model.setCreatedBy(currentUser);
            model.setCreatedAt(date);
        }
        model.setModifiedBy(currentUser);
        model.setModifiedAt(date);
    }

    public static String[] splitPostgresStruct(String str) {
        return StreamEx.of(str.substring(1, str.length() - 1).split(","))
                .map(s -> {
                    return !s.isEmpty() && s.charAt(0) == '"'
                            ? s.substring(1, s.length() - 1).replace("\"\"", "\"")
                            : s;
                })
                .toArray(String[]::new);
    }
}
