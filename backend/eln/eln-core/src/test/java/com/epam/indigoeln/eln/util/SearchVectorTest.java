package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.eln.common.util.SearchVector;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchVectorTest {

    @Test
    void testReorderedValuesAreEqual() {
        SearchVector original = new SearchVector.Builder().a("p1").b("k1").b("k2").d("description").build();
        SearchVector reordered = new SearchVector.Builder().d("description").b("k2").b("k1").a("p1").build();
        assertThat(reordered).isEqualTo(original).hasSameHashCodeAs(original);
    }

    @Test
    void testDifferentValuesAreNotEqual() {
        SearchVector original = new SearchVector.Builder().a("p1").b("k1").build();
        assertThat(new SearchVector.Builder().a("p1").b("k2").build()).isNotEqualTo(original);
        // same text under a different weight is a different vector
        assertThat(new SearchVector.Builder().a("p1").c("k1").build()).isNotEqualTo(original);
    }

    @Test
    void testDuplicatesAndEmptyValuesAreDropped() {
        SearchVector vector = new SearchVector.Builder().b("k1").b("k1").b("").b(null).build();
        assertThat(vector.getB()).containsExactly("k1");
        assertThat(new SearchVector.Builder().build()).isEqualTo(SearchVector.EMPTY);
    }

    @Test
    void testIsImmutable() {
        Set<String> source = new java.util.HashSet<>(Set.of("k1"));
        SearchVector vector = new SearchVector(Set.of(), source, Set.of(), Set.of());
        source.add("k2");
        assertThat(vector.getB()).containsExactly("k1");
        assertThat(vector.getB()).isUnmodifiable();
    }
}
