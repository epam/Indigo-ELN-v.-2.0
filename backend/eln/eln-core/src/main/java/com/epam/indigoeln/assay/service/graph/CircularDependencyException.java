package com.epam.indigoeln.assay.service.graph;

import java.util.List;

/**
 * Thrown when an edge would introduce a cycle into the value dependency graph, or when a
 * cycle is detected while ordering values for recomputation. The {@code cycle} lists the
 * nodes forming the loop (best-effort), to surface a meaningful message to the user.
 */
public class CircularDependencyException extends RuntimeException {

    private final transient List<?> cycle;

    public CircularDependencyException(String message, List<?> cycle) {
        super(message);
        this.cycle = List.copyOf(cycle);
    }

    public List<?> getCycle() {
        return cycle;
    }
}
