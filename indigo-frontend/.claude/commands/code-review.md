---
description: Review code for bugs, best practices, and project conventions
argument-hint: [file path or "staged" / "diff"]
---

Read `.ai/conventions.md` first and use it as the review checklist.

Review the code in: **$ARGUMENTS**
(If no target is given, review the current uncommitted changes via `git diff`.)

Check for:

- **Correctness & bugs:** logic errors, unhandled edge cases, null/undefined access,
  race conditions, unsubscribed observables (missing `takeUntilDestroyed`).
- **Angular conventions:** standalone components, `eln-` selector prefix, signal API
  for new code (`input()`, `output()`, `signal`, `computed`, `inject`), new control-flow
  blocks (`@if`/`@for`) instead of `*ngIf`/`*ngFor`.
- **Services & state:** correct `providedIn` usage, signal-based state, reuse of
  `ApiService<T>` for HTTP, proper RxJS operator usage.
- **Types:** no `any` (ESLint warns on it), interfaces in `*.i.ts` files, accurate typing.
- **Styling:** Tailwind utility classes preferred, component style budget respected.
- **Naming & structure:** kebab-case files, correct path aliases (`@core`, `@app`, etc.).
- **Lint/format:** Prettier rules (single quotes, width 120, trailing commas, semicolons).
- **Tests:** is the change covered? Suggest spec additions where valuable.

Output format:

- Group findings by severity: **Critical**, **Warning**, **Suggestion**.
- For each finding, reference `file:line` and give a concrete fix (show a snippet).
- End with a short summary and an overall assessment.
- Do NOT modify files unless explicitly asked — this is a review.
