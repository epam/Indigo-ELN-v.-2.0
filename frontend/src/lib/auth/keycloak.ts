import Keycloak from 'keycloak-js';

import { absoluteUri, KEYCLOAK_INIT_OPTIONS, MIN_TOKEN_VALIDITY_SECONDS } from './keycloak-config';
import type { AuthChange, AuthProvider } from './types';

/**
 * The Keycloak provider, used by the docker-compose stack (see deployment-compose).
 *
 * Unlike Cognito there is no in-app credential form: Keycloak's own login page collects the
 * password, so `components/auth/login-card.tsx` is never reached in this configuration. That
 * matches indigo-frontend's `local` build, whose app shell was a bare `<router-outlet>`.
 */
export async function createKeycloakProvider(config: {
  keycloakUrl: string;
  keycloakRealm: string;
  keycloakClientId: string;
}): Promise<AuthProvider> {
  const keycloak = new Keycloak({
    url: config.keycloakUrl,
    realm: config.keycloakRealm,
    clientId: config.keycloakClientId,
  });

  const listeners = new Set<(event: AuthChange) => void>();
  const emit = (event: AuthChange) => {
    for (const listener of listeners) listener(event);
  };

  keycloak.onAuthSuccess = () => emit('signedIn');
  keycloak.onAuthLogout = () => emit('signedOut');
  // A refresh that cannot be completed is an ended session, not a transient error: the cache has
  // to be dropped, or the app sits on another user's data behind a wall of 401s.
  keycloak.onAuthRefreshError = () => emit('signedOut');

  // Swallowed rather than rethrown: with Keycloak unreachable, a rejection here would blank the
  // page at the top-level await in main.tsx. Carrying on leaves no session, so the `_auth` guard
  // redirects to Keycloak and the browser shows its own connection error — which names the
  // actual problem.
  await keycloak.init(KEYCLOAK_INIT_OPTIONS).catch((error: unknown) => {
    console.error('Keycloak initialisation failed', error);
    return false;
  });

  return {
    async getAccessToken() {
      if (!keycloak.authenticated) return undefined;
      try {
        await keycloak.updateToken(MIN_TOKEN_VALIDITY_SECONDS);
      } catch {
        // The session has ended; onAuthRefreshError has already told the cache to drop.
        return undefined;
      }
      return keycloak.token;
    },

    async getUserId() {
      return keycloak.tokenParsed?.sub;
    },

    async requireLogin(returnTo: string) {
      // BASE_URL, because `returnTo` is a router href and the router's basepath is that same
      // value: the deep link has to be put back under the base for Keycloak to return the user
      // to a path this app is actually served from.
      await keycloak.login({
        redirectUri: absoluteUri(returnTo, window.location.origin, import.meta.env.BASE_URL),
      });
      // login() has started a top-level navigation. Block here rather than returning, so the
      // guard does not let the authenticated shell mount — it would fire an unauthenticated
      // currentUser request and flash a 401 toast in the moment before the browser leaves.
      await new Promise<never>(() => {});
      return true;
    },

    async signOut() {
      await keycloak.logout({
        redirectUri: absoluteUri('/', window.location.origin, import.meta.env.BASE_URL),
      });
    },

    onAuthChange(listener: (event: AuthChange) => void) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
  };
}
