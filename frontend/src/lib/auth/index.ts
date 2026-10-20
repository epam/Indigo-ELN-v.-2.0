import { loadConfig } from '@/lib/env';

import { createCognitoProvider } from './cognito';
import { createKeycloakProvider } from './keycloak';
import type { AuthChange, AuthProvider } from './types';

/**
 * The identity provider this environment uses, chosen from the runtime config.
 *
 * It cannot be a plain module-level constant the way a build-time flag would allow: `config.json`
 * is fetched, so the choice is only known once `initAuth` has awaited it. The exported `auth`
 * below is therefore a thin delegator, which keeps every call site a plain synchronous import.
 */
let active: AuthProvider | undefined;

/** Called once from main.tsx, before anything reads a token. */
export async function initAuth(): Promise<void> {
  const config = await loadConfig();
  active =
    config.authProvider === 'keycloak' ? await createKeycloakProvider(config) : await createCognitoProvider(config);
}

function provider(): AuthProvider {
  if (!active) {
    throw new Error('Auth has not been initialised — initAuth() must finish before any auth call');
  }
  return active;
}

export const auth: AuthProvider = {
  getAccessToken: () => provider().getAccessToken(),
  getUserId: () => provider().getUserId(),
  requireLogin: (returnTo: string) => provider().requireLogin(returnTo),
  signOut: () => provider().signOut(),
  onAuthChange: (listener: (event: AuthChange) => void) => provider().onAuthChange(listener),
};

export type { AuthChange, AuthProvider } from './types';
