import { describe, expect, it } from 'vitest';

import { absoluteUri, KEYCLOAK_INIT_OPTIONS } from './keycloak-config';

describe('KEYCLOAK_INIT_OPTIONS', () => {
  it('uses check-sso, so an unauthenticated visitor reaches the guard instead of being redirected at bootstrap', () => {
    // This is what preserves the deep link: login-required would redirect before the router ran.
    expect(KEYCLOAK_INIT_OPTIONS.onLoad).toBe('check-sso');
  });

  it('keeps the login-status iframe off, which the CSP would block', () => {
    // default-src 'self' with no frame-src means Keycloak's iframe cannot load; check-sso then
    // falls back to a top-level prompt=none redirect. See vite.config.ts buildPolicy().
    expect(KEYCLOAK_INIT_OPTIONS.checkLoginIframe).toBe(false);
  });

  it('requires PKCE, since frontend-client is a public client and cannot hold a secret', () => {
    expect(KEYCLOAK_INIT_OPTIONS.pkceMethod).toBe('S256');
  });

  it('configures no silent-SSO redirect, which is what makes check-sso use a full redirect', () => {
    expect(KEYCLOAK_INIT_OPTIONS.silentCheckSsoRedirectUri).toBeUndefined();
  });
});

describe('absoluteUri', () => {
  it('resolves a router href against the origin when the app is served from the root', () => {
    expect(absoluteUri('/projects', 'http://localhost', '/')).toBe('http://localhost/projects');
  });

  it('puts a basepath back on a router href', () => {
    // The app is served from the root today, so this guards the other case: the router strips the
    // basepath, and resolving '/projects' against the bare origin would send Keycloak's callback
    // to a path nginx does not serve — a 404 instead of the app.
    expect(absoluteUri('/projects', 'http://localhost', '/app/')).toBe('http://localhost/app/projects');
  });

  it('keeps the query string a deep link carries', () => {
    expect(absoluteUri('/projects?sort=LATEST&view=grid', 'http://localhost', '/')).toBe(
      'http://localhost/projects?sort=LATEST&view=grid',
    );
  });

  it('maps the app root onto the basepath', () => {
    expect(absoluteUri('/', 'http://localhost', '/')).toBe('http://localhost/');
  });

  it('preserves a non-default port, so a published-port deployment still matches its allowlist', () => {
    expect(absoluteUri('/', 'http://localhost:4173', '/')).toBe('http://localhost:4173/');
  });

  it('leaves an already-absolute URL alone', () => {
    expect(absoluteUri('http://localhost/projects', 'http://localhost', '/')).toBe('http://localhost/projects');
  });
});
