package com.epam.indigoeln.reaction.util;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class StreamUtil {

    /**
     * With JSpecify, it's not possible to express that Stream.filter(Objects::nonNull) or StreamEx.nonNull() returns a stream of non-null values.
     * In a common case stream.nonNull().toList(), use this function.
     */
    public static <T> Collector<@Nullable T, ?, List<T>> toListNotNull() {
        return Collector.of(
                ArrayList::new,
                (acc, item) -> {
                    if (item != null) {
                        acc.add(item);
                    }
                },
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }
}
