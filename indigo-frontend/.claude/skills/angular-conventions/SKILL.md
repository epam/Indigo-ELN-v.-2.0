---
name: angular-conventions
description: Angular 19 standalone component and TypeScript conventions for the Indigo ELN frontend. Use when creating or editing Angular components, directives, pipes, or any *.component.ts / *.component.html files, or when deciding between signals vs decorators, control-flow syntax, DI style, or file/selector naming.
---

# Angular Conventions (Indigo ELN Frontend)

Angular 19.1, standalone-only, zone-based change detection. No `NgModule`.
Full reference: `.ai/conventions.md`.

## Components

- Selector: `eln-` prefix, kebab-case, element type — ESLint-enforced.
  e.g. `selector: 'eln-experiment-details'`.
- Always set `standalone: true` explicitly.
- Use a separate `templateUrl`; only add `styleUrl` (singular) when component styles
  are truly needed — prefer Tailwind utility classes in the template.
- Declare deps in `imports: [...]` (CommonModule, Material modules, other standalone
  components/pipes).

### Prefer the modern signal API for new code

```ts
import { Component, DestroyRef, computed, inject, input, output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-example',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './example.component.html',
})
export class ExampleComponent {
  id = input.required<string>(); // signal inputs
  variant = input<'blue' | 'grey'>('grey');
  changed = output<string>(); // signal outputs

  private exampleService = inject(ExampleService); // inject(), not constructor DI
  private destroyRef = inject(DestroyRef);

  data = computed(() => this.exampleService.data()); // derived state

  constructor() {
    this.someStream$
      .pipe(takeUntilDestroyed(this.destroyRef)) // cleanup
      .subscribe();
  }
}
```

- Inputs: `input()` / `input.required<T>()`; Outputs: `output()`.
- State: `signal()`, `computed()`, `effect()` (effects in the constructor).
- DI: `inject()`. Cleanup: `takeUntilDestroyed(inject(DestroyRef))`.

### Legacy components

Older `common/` components use `@Input()` / `@Output() x = new EventEmitter<T>()`.
The codebase is mid-migration. **When editing an existing file, match its dominant
style**; reach for the signal API for genuinely new code or low-risk changes.

## Templates

- Use new control-flow blocks — never `*ngIf` / `*ngFor`:
  ```html
  @if (data()) {
  <span>{{ data().name }}</span>
  } @for (item of items(); track item.id) {
  <li>{{ item.label }}</li>
  }
  ```
- Tailwind utility classes for layout/styling (`class="flex items-center gap-2"`).
- Custom icon font classes look like `indicon-plus`, `indicon-sort`.
- Use `| async` for RxJS streams.

## Files, naming, imports

- kebab-case filenames with type suffixes: `*.component.ts/.html/.scss/.spec.ts`,
  `*.service.ts`, `*.guard.ts`, `*.pipe.ts`, `*.directive.ts`, `*.interceptor.ts`.
- Interfaces/types live in `*.i.ts` files (e.g. `experiment-detail.i.ts`).
- One component per folder, folder named after the component.
- Feature pages under `src/app/pages/`; shared code under `src/core/`.
- Path aliases: `@/*`→src, `@app/*`→src/app, `@pages/*`→src/app/pages, `@core/*`→src/core
  (core is at `src/core`, NOT `src/app/core`).

## Lint / format (must pass; lefthook runs on commit)

- Avoid `any` (`@typescript-eslint/no-explicit-any` warns). Prefix intentionally
  unused vars with `_`.
- Prettier: single quotes, trailing comma `all`, `printWidth: 120`, semicolons,
  `arrowParens: always`, 2-space indent, LF.
- Verify with `npm run build` and `npm run prettier`.
