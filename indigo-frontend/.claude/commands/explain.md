---
description: Explain a piece of code, component, or feature in the codebase
argument-hint: <file path, symbol, or feature name>
---

Read `.ai/conventions.md` for project context first.

Explain: **$ARGUMENTS**

Provide a clear, concise explanation aimed at a developer new to this part of the code:

1. **Purpose** — what this code/feature does and the problem it solves.
2. **How it works** — the main flow, key methods, signals/state, and data sources
   (which services/`ApiService` endpoints it touches).
3. **Inputs/outputs** — component `input()`/`output()` (or `@Input`/`@Output`),
   public service methods, and important types (from `*.i.ts`).
4. **Dependencies & collaborators** — what it injects and what depends on it.
5. **Gotchas** — edge cases, side effects, subscriptions, or non-obvious behavior.

Guidelines:

- Reference concrete locations as `file:line` so the reader can navigate.
- Trace real call paths by reading the relevant files; don't guess.
- Keep it focused; use short code snippets only when they aid understanding.
- Do NOT modify any files — this is an explanation only.
