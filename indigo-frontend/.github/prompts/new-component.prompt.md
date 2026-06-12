---
mode: agent
description: Scaffold a new standalone Angular component following project conventions
---

Follow the conventions in [conventions.md](../../.ai/conventions.md).

Create a new standalone Angular component. Ask me for the component name and target
location if they aren't provided.

Requirements:

- Selector with the `eln-` prefix, kebab-case (e.g. `eln-my-thing`).
- `standalone: true`, separate `templateUrl`, no `styleUrl` unless styles are needed.
- Modern signal API: `input()` / `input.required<T>()`, `output()`, `signal()`,
  `computed()`, `inject()` for DI, and `takeUntilDestroyed(inject(DestroyRef))` for
  subscriptions.
- Template uses new control-flow blocks (`@if`, `@for (... ; track ...)`) and Tailwind
  utility classes — never `*ngIf` / `*ngFor`.
- Correct location: feature → `src/app/pages/...`, shared → `src/core/components/...`;
  one component per folder named after the component; kebab-case filenames.
- Put interfaces/types in a `*.i.ts` file. Use path aliases (`@core`, `@app`, `@pages`, `@/`).

Create the `.component.ts` and `.component.html` files, then verify with `npm run build`.
