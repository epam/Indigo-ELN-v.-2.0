---
mode: agent
description: Scaffold a new Angular service following project conventions
---

Follow the conventions in [conventions.md](../../.ai/conventions.md).

Create a new Angular service. Ask me for the service name and whether it should be a
singleton or component-scoped if not provided.

Requirements:

- `@Injectable({ providedIn: 'root' })` for app-wide singletons, or `@Injectable()`
  (no `providedIn`) for component-scoped services.
- Use `inject()` for dependencies (not constructor DI).
- For HTTP, reuse the generic `ApiService<T>` from `@core/services` (it prefixes
  `/api/eln/`). Avoid calling `HttpClient` directly without a clear reason.
- Manage state with signals:
  - `readonly data = signal<T | null>(null);`
  - `readonly derived = computed(() => ...);`
  - `readonly isLoading = signal(false);`
  - events via `readonly changed$ = new Subject<void>();`
- Use RxJS operators (`tap`, `switchMap`, `finalize`, `catchError`) for side effects.
- Name the file `*.service.ts`; put types in a `*.i.ts` file; use path aliases.

Create the service (and any `*.i.ts` types), then verify with `npm run build`.
