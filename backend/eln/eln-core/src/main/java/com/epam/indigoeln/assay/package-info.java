/**
 * Assay data registration (microtitre plate) feature.
 *
 * <p>Provides a first-class, cross-experiment model for plate-based assay data:
 * <ul>
 *   <li>{@code entity} - JPA entities for assays, plates, wells, well contents, values,
 *       value scopes and conflicts (schema in Flyway migration {@code V1.0.105__assays.sql});</li>
 *   <li>{@code service.graph} - the value dependency graph: circular-dependency detection and
 *       topological recomputation ordering for change propagation;</li>
 *   <li>{@code service.calc} - the calculation engine: a library of built-in calculations plus a
 *       sandboxed custom-formula evaluator.</li>
 * </ul>
 *
 * <p>Every input or output of a calculation is an {@link com.epam.indigoeln.assay.entity.AssayValueEntity},
 * so raw readings, well-content concentrations and derived results share one uniform dependency
 * graph. Values in a signed/locked experiment are frozen; propagation that would change them
 * records a {@link com.epam.indigoeln.assay.entity.ValueConflictEntity} for human review instead.
 */
@NullMarked
package com.epam.indigoeln.assay;

import org.jspecify.annotations.NullMarked;
