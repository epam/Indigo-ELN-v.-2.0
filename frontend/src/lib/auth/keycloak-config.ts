import type { KeycloakInitOptions } from 'keycloak-js';

/**
 * The Keycloak choices worth asserting, kept in their own module so they can be tested without
 * constructing a client or loading the runtime config. indigo-frontend keeps `keycloak.config.ts`
 * out of its fileReplacements set for exactly this reason.
 */

/**
 * Seconds of remaining validity below which `updateToken` refreshes instead of reusing the
 * current token.
 */
export const MIN_TOKEN_VALIDITY_SECONDS = 30;

/**
 * `check-sso` rather than `login-required`: an unauthenticated visitor should reach the app and
 * let the `_auth` guard decide, which is what preserves the deep link they asked for. The Angular
 * app redirects during bootstrap instead, and so always lands on the app root.
 *
 * `checkLoginIframe` must stay off. The CSP sets `default-src 'self'` with no `frame-src`, so
 * Keycloak's login-status iframe is blocked; with no `silentCheckSsoRedirectUri` configured,
 * `check-sso` falls back to a top-level `prompt=none` redirect, which CSP does not police.
 */
export const KEYCLOAK_INIT_OPTIONS: KeycloakInitOptions = {
  onLoad: 'check-sso',
  pkceMethod: 'S256',
  checkLoginIframe: false,
};

/**
 * Keycloak needs an absolute `redirect_uri` for its client allowlist to match, while the router
 * hands out hrefs with the basepath already stripped.
 *
 * The leading slash has to go before resolving: `new URL('/projects', 'http://host/base/')`
 * treats an absolute path as rooted and answers `http://host/projects`, dropping the base. That
 * lands the OIDC callback on a path nginx may not serve, so the login ends on a 404.
 */
export function absoluteUri(target: string, origin: string, basePath = '/'): string {
  return new URL(target.replace(/^\/+/, ''), new URL(basePath, origin)).href;
}
