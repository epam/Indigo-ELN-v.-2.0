/**
 * Stands in for `aws-amplify/auth` inside Storybook (aliased in main.ts).
 * Amplify is never configured there, so the real module would throw.
 */
export async function fetchAuthSession() {
  return { tokens: { accessToken: { toString: () => 'storybook-token' } } };
}

export async function signOut() {}
