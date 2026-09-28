FROM node:24.20.0-slim AS build

WORKDIR /app

RUN npm install -g pnpm@11.3.0

# pnpm-workspace.yaml carries allowBuilds, which esbuild's postinstall needs.
# The store is pinned so the cache mount below can't drift from pnpm's default.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,id=pnpm,target=/pnpm/store \
    pnpm install --frozen-lockfile --store-dir=/pnpm/store

COPY . .

# Vite inlines VITE_* at build time; these cannot be set on the runtime container.
ARG VITE_COGNITO_USER_POOL_ID=us-east-1_bgZNcyeDz
ARG VITE_COGNITO_CLIENT_ID=3bqu2iod15b0otft0ap8umlfgh
ENV VITE_COGNITO_USER_POOL_ID=$VITE_COGNITO_USER_POOL_ID \
    VITE_COGNITO_CLIENT_ID=$VITE_COGNITO_CLIENT_ID

RUN pnpm run build

# Reuse the policy the build emitted, so it matches the one CloudFront serves.
RUN node -e "const {policy}=JSON.parse(require('fs').readFileSync('dist/csp-hashes.json','utf8'));require('fs').writeFileSync('csp.conf','add_header Content-Security-Policy \"'+policy+'\" always;\n')"


FROM nginx:alpine

# vite.config.ts sets base: '/frontend2/'; assets resolve under that prefix.
COPY --from=build /app/dist /usr/share/nginx/html/frontend2
COPY --from=build /app/csp.conf /etc/nginx/snippets/csp.conf
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
