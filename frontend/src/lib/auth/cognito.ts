import { Amplify } from 'aws-amplify';
import { fetchAuthSession, signOut as amplifySignOut } from 'aws-amplify/auth';
import { Hub } from 'aws-amplify/utils';

import { applyStoredSessionPersistence, clearStoredTokens } from '@/lib/auth-storage';

import type { AuthChange, AuthProvider } from './types';

/**
 * The AWS Cognito provider — what every environment used before Keycloak was an option, and still
 * the default. Credentials are entered in-app by `components/auth/login-card.tsx`, which talks to
 * Amplify's headless API directly; this module owns everything around that.
 */
export async function createCognitoProvider(config: {
  cognitoUserPoolId: string;
  cognitoClientId: string;
}): Promise<AuthProvider> {
  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId: config.cognitoUserPoolId,
        userPoolClientId: config.cognitoClientId,
      },
    },
  });
  // Ordering is load-bearing: main.tsx resolves a session immediately after this returns, and that
  // read has to reach the store the tokens were actually written to. See auth-storage.ts.
  applyStoredSessionPersistence();

  return {
    async getAccessToken() {
      const session = await fetchAuthSession().catch(() => null);
      return session?.tokens?.accessToken?.toString();
    },

    async getUserId() {
      const session = await fetchAuthSession().catch(() => null);
      return session?.userSub;
    },

    async requireLogin() {
      // Cognito's credential UI is a route in this app, so the caller does the redirecting.
      return false;
    },

    async signOut() {
      await amplifySignOut();
      // signOut() empties only the store Remember me currently points at, so a session written to
      // the other one would outlive the sign-out. See auth-storage.ts.
      clearStoredTokens();
    },

    onAuthChange(listener: (event: AuthChange) => void) {
      return Hub.listen('auth', ({ payload }) => {
        if (payload.event === 'signedIn') listener('signedIn');
        if (payload.event === 'signedOut') listener('signedOut');
      });
    },
  };
}
