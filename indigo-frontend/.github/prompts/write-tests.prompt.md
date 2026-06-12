---
mode: agent
description: Generate Jasmine/Karma unit tests for a component or service
---

Follow the conventions in [conventions.md](../../.ai/conventions.md) (testing section).

Write unit tests for the selected file (or the file I specify).

Requirements:

- Use **Jasmine + Karma** with `TestBed` (this project does NOT use Jest/Vitest).
- Name the file `*.spec.ts`, colocated next to the file under test.
- Standalone **component**: import it directly in `TestBed`:
  ```ts
  await TestBed.configureTestingModule({ imports: [TheComponent] }).compileComponents();
  const fixture = TestBed.createComponent(TheComponent);
  ```
  Set required signal inputs with `fixture.componentRef.setInput('name', value)`.
- **Service with HTTP**: provide `provideHttpClient()` + `provideHttpClientTesting()`,
  then use `HttpTestingController` (`expectOne`, `flush`, `afterEach(() => httpMock.verify())`).
- Mock collaborators with `jasmine.createSpyObj` and provide them via the testing module.
- Cover creation, key public methods, computed/derived values, emitted outputs, and
  meaningful edge cases — not just the boilerplate `should create`.
- Group related cases with nested `describe` blocks.

Read the target file first, write the spec, then run `npm test` and ensure it passes.
