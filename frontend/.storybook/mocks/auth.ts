/**
 * Stands in for `@/lib/auth` inside Storybook (aliased in main.ts).
 *
 * Neither provider is usable there: config.json is never fetched, Amplify is never configured and
 * Keycloak has no server, so the real facade would throw on the first `apiFetch`. A story is
 * always signed in, and MSW owns the network.
 */
// Relative, not '@/lib/auth/types': the '@/lib/auth' alias in main.ts matches by prefix and would
// rewrite that specifier to `<this file>/types`.
import type { AuthProvider } from '../../src/lib/auth/types';

export const auth: AuthProvider = {
  getAccessToken: async () => 'storybook-token',
  getUserId: async () => 'storybook-user',
  requireLogin: async () => false,
  signOut: async () => {},
  onAuthChange: () => () => {},
};

export async function initAuth(): Promise<void> {}
