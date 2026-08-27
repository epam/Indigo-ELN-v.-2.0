# Indigo ELN Frontend — Project Conventions

This is the single source of truth for coding conventions in this project. Both the
Claude commands (`.claude/commands/`) and GitHub Copilot prompts (`.github/prompts/`)
reference these rules. Keep this file up to date — the prompts inherit from it.

## Stack

- **Angular 19.1**, standalone-only architecture (no `NgModule`).
- **TypeScript ~5.7**, RxJS ~7.8, zone-based change detection.
- **Tailwind CSS v4** (utility-first) + SCSS globals + Angular Material + AWS Amplify UI.
- **No NgRx** — state lives in signal-holding services.
- Tests: **Jasmine + Karma** (`*.spec.ts`, `TestBed`).
- Lint/format: **ESLint** (flat config) + **Prettier**, enforced via **lefthook** pre-commit.

## Components

- Selector uses the **`eln-` prefix**, kebab-case, element type (ESLint-enforced).
  e.g. `selector: 'eln-experiment-details'`.
- Always set `standalone: true` explicitly.
- Use **separate template files** (`templateUrl`), not inline templates.
- Only add `styleUrl` (singular) when component-specific styles are truly needed —
  prefer Tailwind utility classes in the template.
- List dependencies in `imports: [...]` (CommonModule, Material modules, other
  standalone components/pipes).
- **Prefer the modern signal API for new code:**
  - Inputs: `input()` / `input.required<T>()`
  - Outputs: `output()`
  - State: `signal()`, `computed()`, `effect()`
  - DI: `inject()` (not constructor DI)
  - Cleanup: `takeUntilDestroyed(inject(DestroyRef))`
- Legacy `common/` components use `@Input()` / `@Output() = new EventEmitter<T>()`.
  When editing an existing file, **match the surrounding file's style**.

```ts
@Component({
  selector: 'eln-example',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './example.component.html',
})
export class ExampleComponent {
  id = input.required<string>();
  changed = output<string>();

  private exampleService = inject(ExampleService);
  private destroyRef = inject(DestroyRef);

  data = computed(() => this.exampleService.data());
}
```

## Templates

- Use the **new control flow** blocks: `@if`, `@for (item of items; track item.id)`,
  `@switch`. Do NOT use `*ngIf` / `*ngFor`.
- Use Tailwind utility classes for layout/styling (`class="flex items-center gap-2"`).
  Tailwind v4 important suffix is allowed (`m-4!`).
- Custom icon font classes look like `indicon-plus`, `indicon-sort`.
- Use `| async` for RxJS streams.

## Services

- `@Injectable({ providedIn: 'root' })` for app-wide singletons.
- `@Injectable()` (no `providedIn`) for component-scoped services.
- Use `inject()` for dependencies.
- HTTP goes through the generic `ApiService<T>` wrapper (`@core/services`), which
  prefixes `/api/eln/`. Prefer reusing it over calling `HttpClient` directly.
- State management pattern inside services:
  ```ts
  readonly data = signal<Data | null>(null);
  readonly derived = computed(() => this.data()?.field);
  readonly isLoading = signal(false);
  // events
  readonly changed$ = new Subject<void>();
  ```
- Use RxJS operators (`tap`, `switchMap`, `finalize`, `catchError`) for HTTP side effects.

## Files & Naming

- kebab-case filenames with Angular type suffixes:
  `*.component.ts`, `*.component.html`, `*.component.scss`, `*.component.spec.ts`,
  `*.service.ts`, `*.guard.ts`, `*.pipe.ts`, `*.directive.ts`, `*.interceptor.ts`.
- **Interfaces/types live in `*.i.ts` files** (e.g. `experiment-detail.i.ts`).
- One component per folder; folder is named after the component.
- Feature pages live under `src/app/pages/`; shared code under `src/core/`.

## Path Aliases (tsconfig.json)

- `@/*` → `src/*`
- `@app/*` → `src/app/*`
- `@pages/*` → `src/app/pages/*`
- `@core/*` → `src/core/*` (note: core lives at `src/core`, NOT `src/app/core`)

Group imports: internal aliased imports, then `@angular/*`, then third-party, then `@pages/*`.

## Styling

- Tailwind v4 via PostCSS; theme colors in `@theme { ... }` in `src/scss/main.scss`.
- Global SCSS in `src/scss/` (`_variables`, `_typography`, `_icons`, etc.).
- Keep component styles minimal; per-component budget warns at 4kB, errors at 8kB.

## Testing (Jasmine + Karma)

- Component spec: import the standalone component directly in `TestBed`.
  ```ts
  await TestBed.configureTestingModule({ imports: [ExampleComponent] }).compileComponents();
  const fixture = TestBed.createComponent(ExampleComponent);
  ```
- For HTTP: provide `provideHttpClient(), provideHttpClientTesting()` and use
  `HttpTestingController` (`expectOne`, `flush`, `afterEach(() => httpMock.verify())`).
- Name tests `*.spec.ts`, colocated with the file under test.

## Lint / Format Rules

- ESLint: `eln` selector prefix enforced; `@typescript-eslint/no-explicit-any` = warn
  (avoid `any`); unused vars error unless prefixed with `_`.
- Prettier: single quotes, trailing comma `all`, `printWidth: 120`, semicolons,
  `arrowParens: always`, 2-space indent, LF line endings.
- Run `npm run lint` and `npm run prettier` before committing (lefthook also runs them).
