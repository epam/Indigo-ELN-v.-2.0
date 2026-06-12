---
description: Refactor code to follow Angular 19 and project conventions
argument-hint: <file path or area to refactor>
---

Read `.ai/conventions.md` first and follow it strictly.

Refactor: **$ARGUMENTS**

Goals (apply where relevant, without changing behavior):

- Migrate to the modern Angular signal API: `@Input()` → `input()`/`input.required<T>()`,
  `@Output() = new EventEmitter()` → `output()`, manual subscriptions →
  signals/`computed`, and constructor DI → `inject()`.
- Replace `*ngIf`/`*ngFor` with `@if` / `@for (... ; track ...)` control-flow blocks.
- Ensure subscriptions are cleaned up with `takeUntilDestroyed(inject(DestroyRef))`.
- Remove dead code, duplicate logic, and unnecessary `any` (ESLint warns on `any`).
- Extract reusable logic into services or utilities; move interfaces into `*.i.ts` files.
- Normalize imports to path aliases (`@core`, `@app`, `@pages`, `@/`).
- Apply Prettier formatting (single quotes, width 120, trailing commas, semicolons).

Rules:

- **Preserve existing behavior** — this is a refactor, not a rewrite.
- When editing an existing file mid-migration, match the dominant style of that file,
  but prefer the modern signal API where it doesn't introduce risk.
- Make focused, reviewable changes; explain each non-trivial change briefly.

Steps:

1. Read the target and understand current behavior.
2. Propose the refactor plan, then apply it.
3. Run `npm run build` and `npm test` (or report the commands) to confirm nothing broke.
