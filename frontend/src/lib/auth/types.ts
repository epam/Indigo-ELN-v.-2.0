/** What the app learns when a session starts or ends. */
export type AuthChange = 'signedIn' | 'signedOut';

/**
 * The contract both identity providers satisfy.
 *
 * indigo-frontend split Cognito and Keycloak into two same-named `IdentityService` classes swapped
 * by `angular.json` fileReplacements, with nothing checking that the two stayed compatible — the
 * Keycloak one was not even type-checked during a Cognito build. Declaring the shape here means
 * `tsc` rejects a provider that drifts, whichever one the environment selects.
 *
 * Construction is the provider's own business: each is built by a factory that takes the runtime
 * config and finishes any async setup before returning (see `initAuth` in ./index.ts).
 */
export interface AuthProvider {
  /**
   * The bearer token for `/api/*`, or undefined when there is no usable session.
   *
   * Never rejects: a failed read is reported as undefined so the request goes out unauthenticated
   * and comes back 401, which is the recovery path the app already has a toast for (see toast.ts).
   */
  getAccessToken(): Promise<string | undefined>;

  /**
   * A stable per-user id — the `sub` claim under both providers. Scopes the persisted query
   * cache, so a second user on the same browser starts empty rather than restoring the first's.
   */
  getUserId(): Promise<string | undefined>;

  /**
   * Starts a login for a guard that found no session.
   *
   * Resolves to true when the provider has taken over: Keycloak navigates to its own login page,
   * so in practice it does not resolve at all. Resolves to false when the caller should route to
   * `/login` itself, which is how Cognito's in-app credential form is reached.
   */
  requireLogin(returnTo: string): Promise<boolean>;

  /** Ends the session and clears whatever the provider persisted. */
  signOut(): Promise<void>;

  /** Subscribes to session changes. Returns the unsubscribe function. */
  onAuthChange(listener: (event: AuthChange) => void): () => void;
}
