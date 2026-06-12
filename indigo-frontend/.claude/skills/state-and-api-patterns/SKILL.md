---
name: state-and-api-patterns
description: State management and HTTP/API conventions for the Indigo ELN frontend. Use when creating or editing Angular services, managing shared/component state, fetching data, or calling backend endpoints. Covers the signal-holding-service pattern, the generic ApiService<T> wrapper, and RxJS side-effect handling. There is no NgRx in this project.
---

# State & API Patterns (Indigo ELN Frontend)

No NgRx. State lives in **signal-holding services**; HTTP goes through the generic
**`ApiService<T>`**. Full reference: `.ai/conventions.md`.

## Service shape

- `@Injectable({ providedIn: 'root' })` for app-wide singletons.
- `@Injectable()` (no `providedIn`) for component-scoped services (provided at a
  component level).
- Use `inject()` for dependencies, not constructor DI.

## State with signals

Hold writable state in `signal()`, derive with `computed()`, expose events via RxJS
`Subject`. Mutate with `.set()` / `.update()`.

```ts
@Injectable({ providedIn: 'root' })
export class ExampleService {
  private api = inject(ApiService);

  readonly detail = signal<ExampleDetail | null>(null); // writable state
  readonly model = computed(() => this.detail()?.model); // derived state
  readonly isLoading = signal(false);
  readonly hasError = signal(false);

  readonly changed$ = new Subject<void>(); // events
}
```

## Calling the API

`ApiService<T>` (`@core/services/api.service`) wraps `HttpClient` and prefixes
`/api/eln/` automatically (URLs already starting with `/api/` are left as-is). Prefer
it over calling `HttpClient` directly.

Key methods:

```ts
api.request<T>(method, url, body?, options?)  // method: 'get'|'post'|'put'|'delete'|'patch'
api.getPaged(url, pager, filter?)             // -> PaginatedResponse<T>
api.create(url, body)                         // POST
api.update(url, body)                         // PATCH
api.delete(url, id)                           // DELETE /url/id
api.getDictionary<T>(dictionary)              // dictionary lookups
```

URL conventions: pass the resource path without the prefix
(`api.request('get', 'experiments/' + id)` → `/api/eln/experiments/:id`). Pass an
absolute `/api/...` path only when you must bypass the prefix.

## RxJS side-effect pattern

Set loading flags before the call, update state in `tap`, clear flags in `finalize`,
handle failures in the error branch (often reverting to last-known server state).

```ts
load(id: string): void {
  this.isLoading.set(true);
  this.hasError.set(false);

  this.api.request<ExampleDetail>('get', `examples/${id}`).pipe(
    finalize(() => this.isLoading.set(false)),
    tap({
      next: (detail) => this.detail.set(detail),
      error: () => this.hasError.set(true),
    }),
  ).subscribe();
}
```

- Compose dependent calls with `switchMap`; transform with `map`; side effects in `tap`.
- Keep a "last loaded" snapshot (e.g. `structuredClone`) when you need optimistic
  updates with rollback on error (see `ExperimentDetailService` for the canonical example).

## Consuming state in components

Read service signals through a component `computed()`:

```ts
private exampleService = inject(ExampleService);
detail = computed(() => this.exampleService.detail());
```

## Types

Put request/response/entity interfaces in `*.i.ts` files under
`src/core/types/` (`entities/`, `request/`, `response/`). Avoid `any`.

Verify changes with `npm run build`.
