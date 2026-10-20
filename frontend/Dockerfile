FROM node:24.20.0-slim AS build

WORKDIR /app

RUN npm install -g pnpm@11.3.0

# pnpm-workspace.yaml carries allowBuilds, which esbuild's postinstall needs.
# The store is pinned so the cache mount below can't drift from pnpm's default.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,id=pnpm,target=/pnpm/store \
    pnpm install --frozen-lockfile --store-dir=/pnpm/store

COPY . .

# Vite inlines VITE_* into the bundle, so changing one of these needs a rebuild.
# The default is the AWS path, which reads its Cognito settings from /config.json and needs
# nothing here; the three below are used only when the provider is keycloak (src/lib/env.ts).
ARG VITE_AUTH_PROVIDER=cognito
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID

RUN pnpm run build

# Reuse the policy the build emitted, so it matches the one CloudFront serves.
RUN node -e "const {policy}=JSON.parse(require('fs').readFileSync('dist/csp-hashes.json','utf8'));require('fs').writeFileSync('csp.conf','add_header Content-Security-Policy \"'+policy+'\" always;\n')"


FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY --from=build /app/csp.conf /etc/nginx/snippets/csp.conf
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
