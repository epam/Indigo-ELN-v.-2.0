---
description: Scaffold a new standalone Angular component following project conventions
argument-hint: <component-name> [parent path under src/app or src/core]
---

Read `.ai/conventions.md` first and follow it strictly.

Create a new standalone Angular component for: **$ARGUMENTS**

Requirements:

- Selector with the `eln-` prefix, kebab-case (e.g. `eln-my-thing`).
- `standalone: true`, separate `templateUrl`, no `styleUrl` unless styles are needed.
- Use the modern signal API: `input()` / `input.required<T>()`, `output()`,
  `signal()`, `computed()`, and `inject()` for DI. Use
  `takeUntilDestroyed(inject(DestroyRef))` for any subscriptions.
- Use new control-flow blocks (`@if`, `@for ... track`) in the template and
  Tailwind utility classes for layout/styling.
- Place files in the correct folder (feature → `src/app/pages/...`, shared →
  `src/core/components/...`), one component per folder named after the component,
  using kebab-case filenames (`*.component.ts`, `*.component.html`).
- Put any interfaces/types in a `*.i.ts` file.
- Use path aliases (`@core/*`, `@app/*`, `@pages/*`, `@/*`) for imports.

Steps:

1. Ask for the target location only if it's ambiguous; otherwise infer from the name.
2. Create the component `.ts` and `.html` files.
3. If the component needs data, wire it to an existing service via `inject()`.
4. Run `npm run build` (or report the command) to confirm it compiles.
