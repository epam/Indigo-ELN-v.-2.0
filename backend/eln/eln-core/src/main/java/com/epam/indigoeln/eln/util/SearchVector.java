package com.epam.indigoeln.eln.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

// Immutable, with value equality: the search listeners compare vectors to skip rewriting an unchanged one.
// Sets make that comparison ignore order (reordered keywords are not a change); they serialize as JSON arrays
// in insertion order, which is what calculate_tsvector reads.
@Getter
@EqualsAndHashCode
public class SearchVector {

    public static final SearchVector EMPTY = new SearchVector(Set.of(), Set.of(), Set.of(), Set.of());

    private final Set<String> a;
    private final Set<String> b;
    private final Set<String> c;
    private final Set<String> d;

    public SearchVector(Set<String> a, Set<String> b, Set<String> c, Set<String> d) {
        this.a = copyOf(a);
        this.b = copyOf(b);
        this.c = copyOf(c);
        this.d = copyOf(d);
    }

    private static Set<String> copyOf(Set<String> set) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(set));
    }

    public static class Builder {

        private final Set<String> a = new LinkedHashSet<>();
        private final Set<String> b = new LinkedHashSet<>();
        private final Set<String> c = new LinkedHashSet<>();
        private final Set<String> d = new LinkedHashSet<>();

        public SearchVector.Builder a(@Nullable String t) {
            if (StringUtils.isNotEmpty(t)) {
                a.add(t);
            }
            return this;
        }

        public SearchVector.Builder b(@Nullable String t) {
            if (StringUtils.isNotEmpty(t)) {
                b.add(t);
            }
            return this;
        }

        public SearchVector.Builder c(@Nullable String t) {
            if (StringUtils.isNotEmpty(t)) {
                c.add(t);
            }
            return this;
        }

        public SearchVector.Builder d(@Nullable String t) {
            if (StringUtils.isNotEmpty(t)) {
                d.add(t);
            }
            return this;
        }

        public SearchVector build() {
            return new SearchVector(a, b, c, d);
        }
    }
}
