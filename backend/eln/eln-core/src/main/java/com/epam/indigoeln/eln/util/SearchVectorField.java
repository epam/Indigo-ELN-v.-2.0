package com.epam.indigoeln.eln.util;

import org.jspecify.annotations.Nullable;

public record SearchVectorField(String text, char weight) {

    public static @Nullable SearchVectorField a(@Nullable String t) {
        return of(t, 'A');
    }

    public static @Nullable SearchVectorField b(@Nullable String t) {
        return of(t, 'B');
    }

    public static @Nullable SearchVectorField c(@Nullable String t) {
        return of(t, 'C');
    }

    public static @Nullable SearchVectorField d(@Nullable String t) {
        return of(t, 'D');
    }

    private static @Nullable SearchVectorField of(@Nullable String t, char w) {
        return (t == null || t.isEmpty()) ? null : new SearchVectorField(t, w);
    }
}
