# CLAUDE.md

Guidance for AI agents working in this repository.

## What this project is

Indigo ELN — an open-source chemistry Electronic Lab Notebook by EPAM. Chemists
organize work as **Projects → Notebooks → Experiments**; an experiment contains a
reaction scheme (drawn in Ketcher), stoichiometry tables, product batches,
attachments, and can be signed/witnessed via a document-signature workflow.

## Branch landscape — read this first

- **`master`** — active development. Carries the 3.0 rewrite (structure below);
  the former `3.0` branch was promoted to `master` in PR #2. **Default to
  `master` as the base for new work.** (A `3.0` branch may still exist upstream
  or locally; treat it as historical.)
- **`2.x`** — frozen legacy 2.x application. Java 8/Spring Boot monolith
  (`server/`), AngularJS 1.x UI (`ui/`), MongoDB, plus `CRS/`, `bingodb/`, and
  `signature/` services. Only touch it if a task explicitly targets 2.x.

The 2.x folders were removed during the rewrite, so the two branches share
almost no code. Don't cherry-pick between them. Note: the upstream
`epam/Indigo-ELN-v.-2.0` repository still uses the old split (`master` = 2.x,
`3.0` = rewrite), so syncing from upstream means pulling upstream `3.0` into
this repo's `master`.

## 3.0 repository layout

```
backend/              Quarkus (Java) multi-module Gradle build (Kotlin DSL)
  common/             shared code: model, hibernate utils, service & lambda glue, test support
  eln/                main ELN service (eln-api / eln-core / eln-service / eln-lambda)
  signature/          document signing service (same api/core/service/lambda split)
  reports/            PDF/report generation (JasperReports, .jrxml templates)
  database/flyway/    Flyway SQL migrations (V* versioned, R__* repeatable views)
  integrationTests/   integration test modules
  buildSrc/           shared Gradle conventions (Java toolchain lives here)
indigo-frontend/      Angular 19 SPA (Material + Tailwind, Ketcher 3.x editor)
deployment-compose/   local docker-compose stack (Postgres, Keycloak, nginx, traefik)
deployment-aws/       AWS CDK deployment (services also ship as Lambdas)
```

Each backend domain follows `*-api` (DTOs/interfaces) → `*-core` (entities,
repositories, services, REST resources) → `*-service` (runnable Quarkus app) →
`*-lambda` (AWS Lambda packaging). REST resources live in
`backend/eln/eln-core/.../eln/controller/` (Project, Notebook, Experiment,
Compound, Dictionary, Template, GlobalSearch, User, Role).

## Key architectural concepts (3.0)

- **Database**: PostgreSQL with Hibernate; schema managed exclusively by Flyway
  migrations in `backend/database/flyway/`. Calculated/derived data uses SQL
  views (repeatable `R__*` migrations). The 2.x MongoDB is gone.
- **Auth**: Keycloak (OIDC/JWT) end to end — `keycloak-angular` +
  `angular-auth-oidc-client` on the frontend, JWT validation in Quarkus. Local
  realm (`indigo-eln`) is provisioned from
  `deployment-compose/keycloak-config-cli/realm-config.json`.
- **Mutations & audit**: experiment edits are expressed as deterministic,
  auditable *mutations* (see the "Big Audit Refactor"). Undo/redo, conflict
  detection, and edit history are built on this — when changing experiment
  editing logic, look at the mutation handlers in `eln-core` rather than
  mutating entities directly.
- **Chemistry**: the Indigo toolkit wrapper lives in
  `eln-core` (`indigowrapper`, `reaction`, `compound` packages); the frontend
  embeds Ketcher (`indigo-frontend/src/core/components/common/ketcher/`).
- **Frontend structure**: feature pages in `src/app/pages/`
  (project, notebook, experiment, template, dictionary, search, signature);
  shared building blocks in `src/core/` (components, services, interceptors,
  types). Forms make heavy use of `@ngx-formly`.

## Common commands

Backend (from `backend/`). **The build requires JDK 25** — the convention plugin
(`buildSrc/src/main/kotlin/eln-conventions.gradle.kts`) pins Java 25 source/target,
so an older JDK fails with `invalid source release: 25`.

```bash
./gradlew build                          # build + unit tests
./gradlew :eln:eln-service:quarkusBuild  # build the main service
./gradlew :eln:eln-service:quarkusDev    # dev mode with live reload
./gradlew test                           # all unit tests
```

### Getting JDK 25 in a cloud / sandbox session

Direct downloads (e.g. Adoptium) are usually blocked by the network policy, but the
Ubuntu apt mirrors are reachable and carry OpenJDK 25:

```bash
sudo apt-get update                         # the package index is often stale — refresh first
sudo apt-get install -y openjdk-25-jdk-headless   # installs to /usr/lib/jvm/java-25-openjdk-amd64
```

Then run Gradle against it:

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
./gradlew :eln:eln-core:compileJava -Dorg.gradle.java.home="$JAVA_HOME" --no-configuration-cache
```

Pass `--no-configuration-cache`: the Quarkus extension tasks aren't configuration-cache
compatible, and a discarded cache otherwise forces a from-scratch recompile of
`common:eln-quarkus-extension` (which then fails on any non-25 JDK). The first build also
downloads the full dependency tree and can take a couple of minutes.

Frontend (from `indigo-frontend/`):

```bash
npm ci
npm start            # dev server on :4200 (proxy.conf.js points /api at a remote dev backend)
npm run start-local  # serve against local stack config
npm test             # Karma/Jasmine unit tests
npm run lint         # ESLint
npm run prettier     # format
```

Full local stack (from repo root):

```bash
./deploy.sh   # builds backend + runs docker compose: frontend, eln-service, Postgres, Keycloak
./stop.sh
```

- App: `http://localhost` (port 80 via nginx — **not** :8080/:4200, local
  Keycloak tokens won't validate against the proxied remote backend)
- API: `http://localhost:10020`; Keycloak admin: `http://localhost:8088` (admin/admin)
- Seeded users: `testuser1`/`testuser1`, `testuser2`/`testuser2`

## Conventions

- **Formatting/hooks**: lefthook pre-commit runs Prettier and `tsc --noEmit` on
  frontend files (`.lefthook.yml`). Run `npm run prettier` before committing
  frontend changes; keep TypeScript compiling.
- **Branches/PRs**: short-lived branches named after the GitHub issue
  (e.g. `ng-544`, `296-frontend-auth-via-keycloak`), merged into `master` via
  PR. Reference the issue number (`#NNN`) in commit messages.
- **Database changes**: never edit an applied `V*` migration — add a new one.
  View changes go in the matching `R__*` repeatable migration.
- **Backend style**: Gradle Kotlin DSL, Quarkus idioms (CDI, Panache-style
  repositories in `eln/repository`), DTOs in `*-api` modules, mappers in
  `eln/mapper`.
- **Tests**: backend unit tests sit next to modules (`src/test/java`);
  cross-service tests live in `backend/integrationTests/`. Frontend specs are
  `*.spec.ts` beside the component.

## Known build/test issues

- `:eln:eln-core` test sources don't all compile: `NotebookServiceTest` imports
  `Paging` / `SortOrder` from `com.epam.indigoeln.eln.model` instead of
  `com.epam.indigoeln.common.model`. Since Gradle compiles the whole test source set
  together, this currently blocks `:eln:eln-core:test`. To run a single test class,
  launch it via the JUnit platform launcher against the compiled classpath.

## In-progress features

- **Assay data registration** (microtitre plate) — see `ASSAY_REGISTRATION.md` for design,
  status, and verification steps. Flyway schema `V1.0.105__assays.sql`, entities and the
  dependency-graph + calculation engines under `backend/eln/eln-core/.../assay/`.

## Legacy 2.x notes (2.x branch only)

`server/` is Spring Boot + MongoDB (Mongock migrations), built with Maven
(`./mvnw`). `ui/` is AngularJS 1.x built with npm/gulp. `CRS/` (compound
registration), `bingodb/` (chemical search), and `signature/` are separate
services wired together by the root `docker-compose.yml`. Treat all of it as
maintenance-only.
