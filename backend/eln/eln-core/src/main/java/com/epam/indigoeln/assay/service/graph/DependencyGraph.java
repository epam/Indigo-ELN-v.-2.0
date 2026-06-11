package com.epam.indigoeln.assay.service.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure in-memory model of the assay value dependency graph, decoupled from persistence so the
 * graph algorithms can be exercised in isolation. An edge {@code from -> to} means {@code from}
 * is <em>derived from</em> (depends on) {@code to}; raw values and concentrations are leaves
 * with no outgoing edges.
 *
 * <p>Supports the three operations the engine needs:
 * <ul>
 *   <li>{@link #wouldCreateCycle} - guard before persisting a new dependency edge;</li>
 *   <li>{@link #downstreamClosure} - everything that must be recomputed when a value changes;</li>
 *   <li>{@link #recomputationOrder} - that closure, topologically ordered so inputs precede outputs.</li>
 * </ul>
 *
 * @param <T> node identifier type (a value id in practice)
 */
public class DependencyGraph<T> {

    /** node -> the nodes it depends on (its inputs). */
    private final Map<T, Set<T>> dependsOn = new HashMap<>();
    /** node -> the nodes that depend on it (its dependents). */
    private final Map<T, Set<T>> dependents = new HashMap<>();

    public void addNode(T node) {
        dependsOn.computeIfAbsent(node, k -> new LinkedHashSet<>());
        dependents.computeIfAbsent(node, k -> new LinkedHashSet<>());
    }

    /**
     * Records that {@code node} depends on {@code input}. Rejects an edge that would create a
     * cycle (including a self-edge) by throwing {@link CircularDependencyException}.
     */
    public void addDependency(T node, T input) {
        if (node.equals(input)) {
            throw new CircularDependencyException("A value cannot depend on itself: " + node, List.of(node));
        }
        List<T> cycle = findPath(input, node);
        if (cycle != null) {
            cycle.add(input);
            throw new CircularDependencyException(
                    "Adding dependency " + node + " -> " + input + " would create a cycle", cycle);
        }
        addNode(node);
        addNode(input);
        dependsOn.get(node).add(input);
        dependents.get(input).add(node);
    }

    /**
     * @return {@code true} if making {@code node} depend on {@code input} would create a cycle.
     */
    public boolean wouldCreateCycle(T node, T input) {
        return node.equals(input) || findPath(input, node) != null;
    }

    /**
     * All nodes that transitively depend on any node in {@code changed} (excluding the changed
     * nodes themselves) - i.e. the values whose results may need recomputing.
     */
    public Set<T> downstreamClosure(Set<T> changed) {
        Set<T> visited = new LinkedHashSet<>();
        Deque<T> queue = new ArrayDeque<>(changed);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            for (T dependent : dependents.getOrDefault(current, Set.of())) {
                if (visited.add(dependent)) {
                    queue.add(dependent);
                }
            }
        }
        return visited;
    }

    /**
     * The changed nodes plus their downstream closure, ordered so that every node appears after
     * all of its dependencies that are present in the set. Throws {@link CircularDependencyException}
     * if the affected subgraph contains a cycle (a corrupt-graph safeguard).
     */
    public List<T> recomputationOrder(Set<T> changed) {
        Set<T> affected = new HashSet<>(changed);
        affected.addAll(downstreamClosure(changed));
        return topologicalSort(affected);
    }

    /** Topologically sorts the given subset (dependencies first) using Kahn's algorithm. */
    public List<T> topologicalSort(Set<T> subset) {
        Map<T, Integer> inDegree = new HashMap<>();
        for (T node : subset) {
            int deg = 0;
            for (T input : dependsOn.getOrDefault(node, Set.of())) {
                if (subset.contains(input)) {
                    deg++;
                }
            }
            inDegree.put(node, deg);
        }
        Deque<T> ready = new ArrayDeque<>();
        for (Map.Entry<T, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                ready.add(e.getKey());
            }
        }
        List<T> order = new ArrayList<>(subset.size());
        while (!ready.isEmpty()) {
            T node = ready.poll();
            order.add(node);
            for (T dependent : dependents.getOrDefault(node, Set.of())) {
                if (subset.contains(dependent)) {
                    int deg = inDegree.merge(dependent, -1, Integer::sum);
                    if (deg == 0) {
                        ready.add(dependent);
                    }
                }
            }
        }
        if (order.size() != subset.size()) {
            List<T> remaining = new ArrayList<>(subset);
            remaining.removeAll(order);
            throw new CircularDependencyException("Cycle detected among values: " + remaining, remaining);
        }
        return order;
    }

    /** Depth-first search for a path from {@code start} to {@code target} following dependsOn edges. */
    private List<T> findPath(T start, T target) {
        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        Map<T, T> parent = new HashMap<>();
        stack.push(start);
        visited.add(start);
        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (current.equals(target)) {
                List<T> path = new ArrayList<>();
                for (T at = current; at != null; at = parent.get(at)) {
                    path.add(at);
                }
                return path;
            }
            for (T next : dependsOn.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    parent.put(next, current);
                    stack.push(next);
                }
            }
        }
        return null;
    }
}
