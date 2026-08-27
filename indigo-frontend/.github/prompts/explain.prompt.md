---
mode: ask
description: Explain a piece of code, component, or feature in the codebase
---

Use [conventions.md](../../.ai/conventions.md) for project context.

Explain the selected code (or the file/symbol/feature I name) for a developer new to
this part of the codebase:

1. **Purpose** — what it does and the problem it solves.
2. **How it works** — main flow, key methods, signals/state, and data sources
   (which services / `ApiService` endpoints it touches).
3. **Inputs/outputs** — `input()`/`output()` (or `@Input`/`@Output`), public service
   methods, and important types (from `*.i.ts`).
4. **Dependencies & collaborators** — what it injects and what depends on it.
5. **Gotchas** — edge cases, side effects, subscriptions, non-obvious behavior.

Reference concrete `file:line` locations, trace real call paths by reading the files,
and keep it focused. Do not modify any files — explanation only.
