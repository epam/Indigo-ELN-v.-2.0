import { loadConfig } from '@/lib/env';

import type { AuthChange, AuthProvider } from './types';

// Only known after config.json is fetched, so `auth` below delegates to it.
let active: AuthProvider | undefined;

/** Called once from main.tsx. Dynamic imports so only the selected provider is fetched. */
export async function initAuth(): Promise<void> {
  const config = await loadConfig();
  if (config.authProvider === 'keycloak') {
    const { createKeycloakProvider } = await import('./keycloak');
    active = await createKeycloakProvider(config);
  } else {
    const { createCognitoProvider } = await import('./cognito');
    active = await createCognitoProvider(config);
  }
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
