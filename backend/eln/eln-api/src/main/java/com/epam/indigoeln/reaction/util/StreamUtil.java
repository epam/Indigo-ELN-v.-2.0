package com.epam.indigoeln.reaction.util;

import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

public class StreamUtil {

    public static <T>StreamEx<T> filterNotNull(StreamEx<@Nullable T> stream) {
        //noinspection NullableProblems
        return stream.nonNull();
    }
}
