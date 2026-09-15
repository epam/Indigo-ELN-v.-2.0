package com.epam.indigoeln.eln.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SearchVector {

    public static final SearchVector EMPTY = new SearchVector(new String[0], new String[0], new String[0], new  String[0]);

    @Getter
    private final String[] a;
    @Getter
    private final String[] b;
    @Getter
    private final String[] c;
    @Getter
    private final String[] d;

    public static class Builder {

        private final List<String> a = new ArrayList<>();
        private final List<String> b = new ArrayList<>();
        private final List<String> c = new ArrayList<>();
        private final List<String> d = new ArrayList<>();

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
            return new SearchVector(a.toArray(new String[0]), b.toArray(new String[0]), c.toArray(new String[0]), d.toArray(new String[0]));
        }
    }
}
