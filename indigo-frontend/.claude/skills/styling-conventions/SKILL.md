---
name: styling-conventions
description: Styling conventions for the Indigo ELN frontend using Tailwind CSS v4, theme color tokens, and SCSS globals. Use when adding or changing styles, writing component templates with CSS classes, choosing colors, or editing *.scss / src/scss files. Styling is Tailwind-utility-first; component-level styles are minimal.
---

# Styling Conventions (Indigo ELN Frontend)

Tailwind CSS v4 (utility-first) + SCSS globals + Angular Material + AWS Amplify UI.
Full reference: `.ai/conventions.md`.

## Default to Tailwind utilities in templates

Style in the template with utility classes; avoid component stylesheets unless truly
needed (per-component budget warns at 4kB, errors at 8kB).

```html
<div class="flex items-center gap-2 px-4 pb-4">
  <span class="text-neutral-700 truncate">{{ label }}</span>
</div>
```

- Tailwind v4 important suffix is allowed when overriding library styles: `m-4!`, `mb-0!`.
- Custom icon font classes: `indicon-plus`, `indicon-sort`, `indicon-check-circle`, etc.

## Theme color tokens

Colors are defined as theme tokens in `src/scss/main.scss` (`@theme { --color-...: var(--...) }`),
so use the semantic Tailwind color utilities rather than raw hex values:

- `primary-{50..600}`, `primary-alpha-5`, `primary-alpha-10`
- `secondary-{100..600}`, `secondary-alpha-10`
- `neutral`, `neutral-{100..1000}`, `neutral-alpha-10`
- `red-{50..300}` (and other status palettes defined in `main.scss`)

Examples: `text-primary-400`, `bg-neutral-200`, `border-neutral-400`, `text-red-100`.
Prefer these tokens over arbitrary values so theming stays consistent.

## SCSS globals

- Tailwind is imported in SCSS via `@use 'tailwindcss';` (v4 style) — there is no
  `tailwind.config.js`.
- Global styles live in `src/scss/` and are composed in `main.scss`:
  `_variables`, `_typography`, `_icons`, `_animations`, `_notification`, `_reset`,
  `_helpers`. `src/styles.scss` pulls these in.
- Angular Material (azure-blue theme via `mat.theme(...)`) and Amplify UI themes are
  global.

## When to add a component stylesheet

Only when utilities can't express it (complex selectors, animations, deep library
overrides). Then:

- Use `styleUrl: './x.component.scss'` (singular).
- Keep it minimal; reference theme `var(--...)` tokens, not hardcoded colors.
- `angular.json` sets `scss` as the style language.

## Don't

- Don't hardcode hex colors when a theme token exists.
- Don't add large component stylesheets for layout that Tailwind handles.
- Don't introduce a `tailwind.config.js` — this is Tailwind v4 (PostCSS + `@theme`).
