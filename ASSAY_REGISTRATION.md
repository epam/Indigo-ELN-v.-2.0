# Assay Data Registration (microtitre plate)

Registration of assay data against compounds, for data generated in microtitre-plate format.
This document tracks the design and the implementation status. It targets the **3.0** stack
(Quarkus serverless backend on PostgreSQL with Flyway/Hibernate; Angular 19 frontend).

## Model in one paragraph

A **Plate** belongs to an experiment and holds **Wells**; each well has 0..many **Well Contents**
(a registered `Sample` batch, a named control, or free-text — each with an optional concentration).
Measurements, concentrations and derived results are all **Assay Values**, linked by
**Value Dependency** edges into a single directed acyclic graph. A value's **Scope** records what it
represents (a well, a group of wells, a plate, a group of plates, or an arbitrary cross-experiment
group). **Calculations** (built-in library or custom formula) produce derived values. Changing a
value recomputes its downstream closure in topological order; an edit that would create a cycle is
rejected. Values in a signed/locked experiment are **frozen** — propagation that would change them
records a **Value Conflict** for review instead of mutating signed data. **Assays** are reusable
definitions that are snapshotted onto a plate when applied, so later edits don't change old plates.

## Status

### Implemented and verified
- **Database schema** — `backend/database/flyway/src/main/resources/db/migration/V1.0.105__assays.sql`
  (Assay, Calculation, Plate, Well, Well_Content, Assay_Value, Assay_Value_Scope, Value_Dependency,
  Value_Conflict, plus enum types and indices). Follows the existing migration conventions.
- **JPA entities** — `backend/eln/eln-core/.../assay/entity/` and enums in
  `backend/eln/eln-api/.../assay/model/`. Mirror the existing entity conventions
  (`BaseEntity`/`IdentifiableEntity`, `PostgreSQLEnumJdbcType`, JSONB via a `JsonNodeType` user type
  modelled on `ExperimentModelType`). The value dependency graph is a self many-to-many over
  `Value_Dependency`.
- **Dependency graph engine** — `backend/eln/eln-core/.../assay/service/graph/`:
  `DependencyGraph` (cycle detection on edge insert, downstream closure, topological recomputation
  order) and `CircularDependencyException`.
- **Calculation engine** — `backend/eln/eln-core/.../assay/service/calc/`: a `CalculationLibrary`
  with built-in providers (MEAN, SD, CV, PERCENT_INHIBITION, NORMALIZE_TO_CONTROLS, Z_PRIME) and a
  sandboxed `FormulaEvaluator` for custom formulas (`+ - * / ^`, parentheses, named vector inputs,
  whitelisted functions `mean/sum/min/max/sd/count/abs/sqrt/ln/log10/exp/pow`).
- **Tests** — JUnit 5 + AssertJ tests for the graph and calculation engines under
  `backend/eln/eln-core/src/test/.../assay/service/`.

### Verification status (done)
The 3.0 backend's convention plugin (`buildSrc/.../eln-conventions.gradle.kts`) pins **Java 25**
source/target. With `openjdk-25-jdk` installed:
- `./gradlew :eln:eln-core:compileJava` — **BUILD SUCCESSFUL**; the entities and engine compile.
- The assay engine JUnit suite (`DependencyGraphTest`, `FormulaEvaluatorTest`,
  `CalculationLibraryTest`) runs **17/17 green** under the JUnit 5 platform.

Note: compiling the module required one unrelated pre-existing fix on the 3.0 branch — a missing
`import com.epam.indigoeln.eln.entity.UserInfo` in `ExperimentHandlers.java`. The full `:eln:eln-core:test`
source set still has a separate pre-existing breakage unrelated to this feature
(`NotebookServiceTest` imports `Paging`/`SortOrder` from the wrong package), so the assay tests were
executed directly via the JUnit platform launcher against the compiled classpath.

### Not yet implemented (next steps)
- **Repositories** (`assay/repository/`) — Panache repositories incl. recursive-CTE traversal for
  large graphs, and cross-experiment value lookups.
- **Services** — graph recompute orchestration persisting results + frozen-value conflict creation;
  snapshot-on-apply for assay definitions; ingestion (manual upsert, instrument file parsers,
  API/bulk upload).
- **JAX-RS API + DTOs + MapStruct mappers** (`assay/api`, `assay/controller`, `assay/mapper`) —
  mirror `ExperimentAPI`/`ExperimentResource`.
- **IC50/EC50 4PL curve fit** — `Ic50CurveFitProvider` is registered but throws until the non-linear
  regression is implemented.
- **Frontend** (`indigo-frontend/src/app/pages/assay/`) — plate grid (Angular CDK), assay/calculation
  builders (ngx-formly), dose-response plots (**plotly.js**), conflict inbox, import wizard.
- **ACL across experiment boundaries** — policy for derivations that reference values in another
  experiment (read access required; derived value inherits its scope's experiment ACL).

## Verification steps (on a JDK 25 toolchain)
1. `cd backend && ./gradlew :eln:eln-core:compileJava` — compile entities + engine.
2. `./gradlew :eln:eln-core:test --tests 'com.epam.indigoeln.assay.*'` — run the engine tests.
3. Apply migrations against a local Postgres (Flyway task) and confirm the new tables/enums/indices
   are created and existing migrations still pass.
