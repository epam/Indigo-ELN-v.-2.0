# Reusable AI Assets

Shared AI assets for this project so team members get consistent, convention-aware
help from **Claude** and **GitHub Copilot**. There are two complementary pieces:

- **Prompts / commands** — task workflows you invoke on demand (e.g. `/new-component`).
- **Claude Skills** — convention knowledge Claude loads **automatically** when relevant
  (e.g. it pulls in `angular-conventions` whenever you edit a component).

## Layout

```
.ai/
  conventions.md              # Single source of truth for coding conventions
  README.md                   # This file
.claude/commands/             # Claude slash commands (/new-component, etc.)
  new-component.md
  new-service.md
  code-review.md
  write-tests.md
  refactor.md
  explain.md
.claude/skills/               # Claude Skills (auto-loaded knowledge)
  angular-conventions/SKILL.md
  state-and-api-patterns/SKILL.md
  styling-conventions/SKILL.md
.github/prompts/              # GitHub Copilot prompt files (/new-component, etc.)
  new-component.prompt.md
  new-service.prompt.md
  code-review.prompt.md
  write-tests.prompt.md
  refactor.prompt.md
  explain.prompt.md
```

Everything references `.ai/conventions.md`, so update conventions in **one place**.

## Available prompts

| Prompt          | What it does                                                        |
| --------------- | ------------------------------------------------------------------- |
| `new-component` | Scaffold a standalone Angular component (signals, `eln-` selector). |
| `new-service`   | Scaffold a service (signal state, `ApiService<T>`, `inject()`).     |
| `code-review`   | Review code/changes against project conventions.                    |
| `write-tests`   | Generate Jasmine/Karma `*.spec.ts` tests.                           |
| `refactor`      | Migrate to Angular 19 signal API and clean up code.                 |
| `explain`       | Explain a file, component, or feature.                              |

## Using with Claude

Files in `.claude/commands/` become slash commands. In a Claude session at the repo root:

```
/new-component experiment-summary card
/code-review src/app/pages/experiment/experiment-actions/experiment-actions.component.ts
/write-tests src/core/services/api.service.ts
/explain ExperimentDetailService
```

Anything you type after the command is passed in as `$ARGUMENTS`.

## Using with GitHub Copilot

Files in `.github/prompts/` are Copilot prompt files (requires the prompt-files feature
enabled in VS Code: `chat.promptFiles`). In Copilot Chat:

```
/new-component
/code-review
/write-tests
```

Select the relevant code in the editor first; the prompt acts on the selection or the
file/area you describe. Prompts with `mode: agent` can create/edit files; `mode: ask`
prompts (like `explain`) only respond with text.

## Claude Skills (auto-loaded knowledge)

Skills in `.claude/skills/` are **project-scoped** and load automatically when their
`description` matches what you're doing — no need to invoke them. They give Claude this
project's conventions as standing context (and you can still trigger one manually with
`/skill-name`).

| Skill                    | Loads when you…                                                |
| ------------------------ | -------------------------------------------------------------- |
| `angular-conventions`    | Create/edit components, pipes, directives, or `*.component.*`. |
| `state-and-api-patterns` | Write services, manage state, or call backend endpoints.       |
| `styling-conventions`    | Edit templates' CSS classes or `*.scss` / `src/scss` files.    |

How skills differ from prompts:

- **Prompts/commands** = a task you run (“scaffold this”, “review that”).
- **Skills** = knowledge Claude applies on its own while working, loaded on demand so
  they cost almost no context until needed (progressive disclosure).

Skills are committed to the repo, so every teammate using Claude Code gets them
automatically. They are a Claude Code feature; GitHub Copilot uses the `.github/prompts/`
files instead.

## Maintaining these assets

- Keep `conventions.md` current — it is the shared contract everything reads from.
- When adding a new **prompt**, create it in **both** `.claude/commands/` and
  `.github/prompts/` so the experience is consistent across tools.
- When adding a new **skill**, create `.claude/skills/<name>/SKILL.md` with a clear
  `description` that states _what it covers and when to use it_ (that text is how Claude
  decides to load it). Keep the body concise; move long reference material to extra
  files in the skill folder and link them from `SKILL.md`.
- Keep prompts and skills short and imperative; defer detailed rules to `conventions.md`.
