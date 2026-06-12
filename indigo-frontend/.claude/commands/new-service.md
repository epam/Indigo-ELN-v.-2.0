---
description: Scaffold a new Angular service following project conventions
argument-hint: <service-name> [singleton | scoped]
---

Read `.ai/conventions.md` first and follow it strictly.

Create a new Angular service for: **$ARGUMENTS**

Requirements:

- Use `@Injectable({ providedIn: 'root' })` for app-wide singletons, or
  `@Injectable()` (no `providedIn`) for component-scoped services.
- Use `inject()` for dependencies (not constructor DI).
- For HTTP, reuse the generic `ApiService<T>` from `@core/services` (it prefixes
  `/api/eln/`); do not call `HttpClient` directly unless there's a clear reason.
- For state, use the signal pattern:
  - `readonly data = signal<T | null>(null);`
  - `readonly derived = computed(() => ...);`
  - `readonly isLoading = signal(false);`
  - events via `readonly changed$ = new Subject<void>();`
- Use RxJS operators (`tap`, `switchMap`, `finalize`, `catchError`) for side effects.
- Name the file `*.service.ts`, place types in a `*.i.ts` file, and use path aliases.

Steps:

1. Confirm singleton vs component-scoped if not specified.
2. Create the service file and any needed `*.i.ts` types.
3. Run `npm run build` (or report the command) to confirm it compiles.
