# indigo-frontend2

React SPA for Indigo ELN. Pure client-side: no SSR, no server runtime — built to static files and
served from S3 behind CloudFront.

Independent of `indigo-frontend/` (Angular); no shared code.

## Stack

| Concern | Choice |
| --- | --- |
| Package manager | pnpm (pinned in `packageManager`) |
| Build | Vite 8 + `@vitejs/plugin-react` |
| Routing | TanStack Router (file-based, `src/routes/`) |
| Server state | TanStack Query |
| Forms | TanStack Form (validated with Zod via Standard Schema) |
| Styling | Tailwind CSS v4 |
| Components | shadcn/ui on Base UI (`components.json`) |
| Auth | Amazon Cognito via `aws-amplify` |
| Tests | Vitest + Testing Library |

## Getting started

```bash
nvm use            # Node 24.20.0 (.nvmrc)
pnpm install
cp .env.example .env.local
pnpm run dev       # http://localhost:5173
```

`pnpm run dev` proxies `/api` to the shared dev backend at
`indigo-eln-dev.test.lifescience.opensource.epam.com`. That must stay the CloudFront domain rather
than the API Gateway URL, because CloudFront injects the `X-API-Secret` header the API origin
expects. Sign in with a Cognito user from the pool in `.env.example`.

## Scripts

| Script | Purpose |
| --- | --- |
| `pnpm run dev` | Dev server with API proxy |
| `pnpm run build` | Typecheck then production build to `dist/` |
| `pnpm run preview` | Serve the production build locally |
| `pnpm run lint` | ESLint |
| `pnpm run format` | Prettier write |
| `pnpm run test` | Vitest (single run) |
| `pnpm run test:stories:native` | Story tests against a natively installed Chromium (no root needed) |

## Conventions

- **Routes** live in `src/routes/` and are compiled to `src/routeTree.gen.ts` by
  `@tanstack/router-plugin` (generated, git-ignored). `_auth.tsx` is a pathless layout route that
  guards everything beneath it on a Cognito session.
- **API calls** go through `apiFetch` in `src/lib/api.ts`. The path is sent verbatim, so callers pass
  the full path (e.g. `'/api/eln/projects'`). It attaches the Cognito **access token** — not the ID
  token, because the backend reads the `username` claim which only access tokens carry.
- **UI components** are added with `pnpm dlx shadcn@latest add <name>` into `src/components/ui/`.
- Import alias `@/` → `src/`.

## Deploying

Served at `https://<domain>/frontend2/`, from the same CloudFront distribution as the Angular app.
The CDK stack uploads a **locally built** `dist/` — exactly as it does for the Angular app — so
`pnpm run build` has to run before `deployment-aws/deploy.sh indigoeln-dev`.

Three pieces make that work, all in `deployment-aws`:

- **`base: '/frontend2/'`** (here, in `vite.config.ts`) puts every asset URL under the prefix and
  gives the router its basepath. `/api` is untouched — it stays root-absolute and same-origin.
- **`CloudFrontStack.java`** adds two behaviours, `/frontend2/assets/*` and `/frontend2*`, ordered
  *before* the Angular app's `*.*` and its catch-all default. A viewer-request function rewrites
  anything without a file extension to `/frontend2/index.html`, which is the deep-link fallback the
  Angular app gets from its Lambda@Edge instead.
- **The CSP is hash-based**, not nonce-based: this build emits no inline script or style, so no
  per-request nonce — and therefore no Lambda@Edge — is needed. `vite build` writes the assembled
  policy to `dist/csp-hashes.json` as `policy`, `vite preview` serves that same string, and the CDK
  bakes it into the viewer-response function at synth time. Changing the policy means editing
  `buildPolicy()` in `vite.config.ts` and redeploying; there is no second copy to keep in sync.
