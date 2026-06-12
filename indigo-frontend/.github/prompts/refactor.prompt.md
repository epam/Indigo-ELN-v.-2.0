---
mode: agent
description: Refactor code to follow Angular 19 and project conventions
---

Follow the conventions in [conventions.md](../../.ai/conventions.md).

Refactor the selected code (or the file/area I specify) without changing behavior.

Goals (apply where relevant):

- Migrate to the modern Angular signal API: `@Input()` → `input()`/`input.required<T>()`,
  `@Output() = new EventEmitter()` → `output()`, manual subscriptions →
  signals/`computed`, constructor DI → `inject()`.
- Replace `*ngIf`/`*ngFor` with `@if` / `@for (... ; track ...)`.
- Ensure subscriptions clean up via `takeUntilDestroyed(inject(DestroyRef))`.
- Remove dead/duplicate code and unnecessary `any`.
- Extract reusable logic into services/utilities; move interfaces into `*.i.ts` files.
- Normalize imports to path aliases; apply Prettier formatting.

Rules:

- **Preserve behavior** — this is a refactor, not a rewrite.
- Match the dominant style of the file being edited, preferring the signal API where safe.
- Make focused, reviewable changes and briefly explain each non-trivial one.

Propose a plan, apply it, then run `npm run build` and `npm test` to confirm.
