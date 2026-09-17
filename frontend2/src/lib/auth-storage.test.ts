import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('aws-amplify/auth/cognito', () => ({
  AUTH_KEY_PREFIX: 'CognitoIdentityServiceProvider',
  cognitoUserPoolsTokenProvider: { setKeyValueStorage: vi.fn() },
}));
vi.mock('aws-amplify/utils', () => ({ defaultStorage: {}, sessionStorage: {} }));

const { clearStoredTokens } = await import('@/lib/auth-storage');

const TOKEN_KEY = 'CognitoIdentityServiceProvider.abc123.jsmith.accessToken';

describe('clearStoredTokens', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  /**
   * The reason this exists: `signOut()` empties only the store Remember me currently points at,
   * so a session written to the other one would otherwise outlive the sign-out.
   */
  it('clears Cognito tokens from both stores, whichever one holds them', () => {
    localStorage.setItem(TOKEN_KEY, 'from-a-remembered-session');
    sessionStorage.setItem(TOKEN_KEY, 'from-a-tab-only-session');

    clearStoredTokens();

    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('leaves everything else alone, the Remember me preference included', () => {
    // The preference is what pre-ticks the checkbox next time; it is not a credential.
    localStorage.setItem('indigo.rememberMe', 'true');
    localStorage.setItem('unrelated', 'keep me');
    localStorage.setItem(TOKEN_KEY, 'token');

    clearStoredTokens();

    expect(localStorage.getItem('indigo.rememberMe')).toBe('true');
    expect(localStorage.getItem('unrelated')).toBe('keep me');
  });

  it('removes every Cognito key, not just the first', () => {
    // Deleting while walking the live key list skips entries, which is why the keys are
    // collected before any of them is removed.
    for (const suffix of ['accessToken', 'idToken', 'refreshToken', 'clockDrift']) {
      localStorage.setItem(`CognitoIdentityServiceProvider.abc123.jsmith.${suffix}`, 'x');
    }

    clearStoredTokens();

    expect(localStorage.length).toBe(0);
  });
});
