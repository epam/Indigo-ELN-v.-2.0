package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.reaction.util.ThrowingRunnable;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ModelUtil {

    public static void updateDates(BaseEntity model, UserEntity currentUser) {
        ZonedDateTime date = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        //noinspection ConstantValue
        if (model.getCreatedAt() == null) {
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

    public static String combinePostgresStruct(@Nullable Object[] parts) {
        StringBuilder sb = new StringBuilder().append('(');
        for (Object part : parts) {
            sb.append(part != null ? part.toString().replace("\"", "\"\"") : "null");
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    @SneakyThrows
    public static <T> T wrapConstraintViolation(Callable<T> function, Function<ConstraintViolationException, @Nullable String> errorMapper) {
        try {
            return function.call();
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            String error = errorMapper.apply(e);
            if (error != null) {
                throw new InvalidRequestException(error);
            }
            throw e;
        }
    }

    public static void wrapConstraintViolation(ThrowingRunnable function, Function<ConstraintViolationException, @Nullable String> errorMapper) {
        wrapConstraintViolation(function.asCallable(), errorMapper);
    }
}
