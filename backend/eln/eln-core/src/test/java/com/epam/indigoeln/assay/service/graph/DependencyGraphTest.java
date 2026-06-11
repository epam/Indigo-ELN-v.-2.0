package com.epam.indigoeln.assay.service.graph;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyGraphTest {

    /** Builds the diamond a -> {b,c} -> d -> e used across the assertions. */
    private DependencyGraph<String> diamond() {
        DependencyGraph<String> g = new DependencyGraph<>();
        g.addDependency("b", "a");
        g.addDependency("c", "a");
        g.addDependency("d", "b");
        g.addDependency("d", "c");
        g.addDependency("e", "d");
        return g;
    }

    @Test
    void detectsCycleBeforeAddingEdge() {
        DependencyGraph<String> g = diamond();
        assertThat(g.wouldCreateCycle("a", "e")).isTrue();
        assertThatThrownBy(() -> g.addDependency("a", "e"))
                .isInstanceOf(CircularDependencyException.class);
    }

    @Test
    void rejectsSelfDependency() {
        DependencyGraph<String> g = new DependencyGraph<>();
        assertThatThrownBy(() -> g.addDependency("x", "x"))
                .isInstanceOf(CircularDependencyException.class);
    }

    @Test
    void computesDownstreamClosure() {
        assertThat(diamond().downstreamClosure(Set.of("a")))
                .containsExactlyInAnyOrder("b", "c", "d", "e");
    }

    @Test
    void ordersRecomputationDependenciesFirst() {
        List<String> order = diamond().recomputationOrder(new HashSet<>(Set.of("a")));
        assertThat(order).hasSize(5);
        assertThat(order.indexOf("a")).isLessThan(order.indexOf("b"));
        assertThat(order.indexOf("b")).isLessThan(order.indexOf("d"));
        assertThat(order.indexOf("c")).isLessThan(order.indexOf("d"));
        assertThat(order.indexOf("d")).isLessThan(order.indexOf("e"));
    }

    @Test
    void recomputesOnlyAffectedSubgraph() {
        assertThat(diamond().recomputationOrder(new HashSet<>(Set.of("d"))))
                .containsExactly("d", "e");
    }
}
