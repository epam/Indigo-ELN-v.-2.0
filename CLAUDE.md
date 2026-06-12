# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Repository layout

The active development line is the **3.0** stack:

- `backend/` — Quarkus serverless (AWS Lambda) services on PostgreSQL, built with Gradle
  (Kotlin DSL), Hibernate ORM, and Flyway migrations. Multi-module: `common`, `database`,
  `eln`, `reports`, `signature`, `integrationTests`.
- `indigo-frontend/` — Angular 19 + TypeScript single-page app.

(The legacy `master` branch uses a different stack — Spring Boot + MongoDB backend under `server/`
and an AngularJS frontend under `ui/`. Don't mix the two.)

## Building the backend — Java 25 is required

The backend's convention plugin (`backend/buildSrc/src/main/kotlin/eln-conventions.gradle.kts`)
pins **Java 25** source/target. A build will fail with `invalid source release: 25` if it runs on
an older JDK.

### Installing JDK 25 in the cloud / sandbox environment

Direct internet downloads (e.g. Adoptium) are typically blocked by the network policy, but the
Ubuntu apt mirrors are reachable and carry OpenJDK 25:

```bash
sudo apt-get update            # the package index is often stale; refresh it first
sudo apt-get install -y openjdk-25-jdk-headless
# installs to /usr/lib/jvm/java-25-openjdk-amd64
```

### Running Gradle against JDK 25

```bash
cd backend
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
./gradlew :eln:eln-core:compileJava -Dorg.gradle.java.home="$JAVA_HOME" --no-configuration-cache
```

Notes:
- Pass `--no-configuration-cache`. The Quarkus extension tasks are not configuration-cache
  compatible; a discarded cache otherwise forces a from-scratch recompile of
  `common:eln-quarkus-extension`, which then fails under any non-25 JDK.
- The first build downloads the full dependency tree and can take a couple of minutes.

## Known pre-existing issues on the 3.0 branch

These are unrelated to any specific feature but block a clean build/test run:

- `:eln:eln-core` test sources don't all compile: `NotebookServiceTest` imports `Paging` /
  `SortOrder` from `com.epam.indigoeln.eln.model` instead of `com.epam.indigoeln.common.model`.
  Because Gradle compiles the whole test source set together, this blocks `:eln:eln-core:test`.
  To run a single test class, launch it directly via the JUnit platform launcher against the
  compiled classpath instead.

## Assay data registration feature

See `ASSAY_REGISTRATION.md` for the design, status, and verification steps of the microtitre-plate
assay registration feature (Flyway schema `V1.0.105__assays.sql`, entities under
`backend/eln/eln-core/.../assay/`, and the dependency-graph + calculation engines).
