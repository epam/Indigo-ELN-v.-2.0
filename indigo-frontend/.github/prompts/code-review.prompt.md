---
mode: agent
description: Review code for bugs, best practices, and project conventions
---

Follow the conventions in [conventions.md](../../.ai/conventions.md) as the review checklist.

Review the selected code (or the file/changes I point you to). If nothing is specified,
review the current uncommitted changes.

Check for:

- **Correctness & bugs:** logic errors, edge cases, null/undefined access, race
  conditions, and unsubscribed observables (missing `takeUntilDestroyed`).
- **Angular conventions:** standalone components, `eln-` selector prefix, signal API
  for new code (`input()`, `output()`, `signal`, `computed`, `inject`), and new
  control-flow blocks (`@if`/`@for`) instead of `*ngIf`/`*ngFor`.
- **Services & state:** correct `providedIn`, signal-based state, reuse of `ApiService<T>`.
- **Types:** no `any`, interfaces in `*.i.ts` files, accurate typing.
- **Styling/naming:** Tailwind utility classes, kebab-case files, correct path aliases.
- **Format:** Prettier rules (single quotes, width 120, trailing commas, semicolons).
- **Tests:** is the change covered? Suggest spec additions.

Output: group findings by **Critical / Warning / Suggestion**, reference `file:line`,
give a concrete fix per finding, and end with a short summary. Do not modify files.
