import { AUTH_KEY_PREFIX, cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import { defaultStorage, sessionStorage } from 'aws-amplify/utils';

/**
 * Where Amplify keeps the Cognito tokens, which is all "Remember me" means.
 *
 * `defaultStorage` is localStorage — the session survives closing the browser. `sessionStorage`
 * is per-tab, so it is gone the moment the tab is. Ported from indigo-frontend's
 * `app.component.ts`, which does the same swap around its own checkbox.
 *
 * One deliberate divergence: the Angular original infers the preference on boot by probing
 * localStorage for a `CognitoIdentityServiceProvider`-prefixed key. An explicit flag is stored
 * here instead. It is the user's actual intent rather than a side effect of it, it survives an
 * expiry that cleared the token keys, and it does not depend on Amplify's internal key naming.
 * The flag is a preference, not a credential.
 *
 * `signOut()` only clears whichever store is active when it runs, so `clearStoredTokens()` below
 * sweeps both — otherwise turning Remember me off would leave the previous session's tokens
 * sitting in localStorage indefinitely.
 */
const REMEMBER_ME_KEY = 'indigo.rememberMe';

/** The stored preference. Defaults to off, which is how the login screen renders the checkbox. */
export function isRemembered(): boolean {
  return localStorage.getItem(REMEMBER_ME_KEY) === 'true';
}

/**
 * Records the preference and points Amplify at the matching store. Must run *before* `signIn`,
 * or the tokens it issues land in whichever store was active beforehand.
 */
export function setSessionPersistence(remember: boolean): void {
  localStorage.setItem(REMEMBER_ME_KEY, String(remember));
  cognitoUserPoolsTokenProvider.setKeyValueStorage(remember ? defaultStorage : sessionStorage);
}

/**
 * Applies the stored preference without changing it. Called from `configureAmplify()`, because
 * `main.tsx` resolves a session before the first render and that read has to reach the store the
 * tokens were actually written to.
 */
export function applyStoredSessionPersistence(): void {
  cognitoUserPoolsTokenProvider.setKeyValueStorage(isRemembered() ? defaultStorage : sessionStorage);
}

/**
 * Removes Cognito's tokens from **both** stores.
 *
 * `signOut()` clears only the store the token provider currently points at, so a session that
 * was written to localStorage under Remember me survives a sign-out taken after the checkbox was
 * turned off — and vice versa. Sweeping both is the only way to be sure nothing is left behind.
 *
 * `AUTH_KEY_PREFIX` is Amplify's own constant (`'CognitoIdentityServiceProvider'`), so this does
 * not hardcode a key shape that could drift; the preference flag is not Cognito's and stays put,
 * since it is what pre-ticks the checkbox next time.
 */
export function clearStoredTokens(): void {
  for (const store of [localStorage, globalThis.sessionStorage]) {
    // Read out with `key(i)` rather than `Object.keys`: the stored entries are exposed through a
    // named-property getter, which `Object.keys` does not report under jsdom — it answers with
    // Storage's own methods instead, so the sweep silently found nothing there. And collected
    // before removing, because deleting while walking the live list skips entries.
    const keys: string[] = [];
    for (let i = 0; i < store.length; i++) {
      const key = store.key(i);
      if (key?.startsWith(AUTH_KEY_PREFIX)) keys.push(key);
    }
    for (const key of keys) store.removeItem(key);
  }
}
