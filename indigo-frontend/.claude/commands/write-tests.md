---
description: Generate Jasmine/Karma unit tests for a component or service
argument-hint: <file path to test>
---

Read `.ai/conventions.md` first and follow the testing section.

Write unit tests for: **$ARGUMENTS**

Requirements:

- Use **Jasmine + Karma** with `TestBed` (this project does NOT use Jest/Vitest).
- Name the file `*.spec.ts`, colocated next to the file under test.
- For a standalone **component**, import it directly:
  ```ts
  await TestBed.configureTestingModule({ imports: [TheComponent] }).compileComponents();
  const fixture = TestBed.createComponent(TheComponent);
  const component = fixture.componentInstance;
  ```
  Set required signal inputs via `fixture.componentRef.setInput('name', value)`.
- For **services that use HTTP**, provide `provideHttpClient()` and
  `provideHttpClientTesting()`, then use `HttpTestingController` with
  `httpMock.expectOne(...)`, `req.flush(...)`, and `afterEach(() => httpMock.verify())`.
- Mock collaborators with jasmine spies (`jasmine.createSpyObj`) and provide them
  via the testing module.
- Cover: creation/`should create`, key public methods, computed/derived values,
  emitted outputs, and meaningful edge cases — not just the boilerplate test.
- Group related cases with nested `describe` blocks.

Steps:

1. Read the target file to understand its public API and dependencies.
2. Write the spec following the patterns above.
3. Run `npm test` (or report the command) and ensure tests pass.
