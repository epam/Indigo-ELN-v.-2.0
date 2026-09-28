/**
 * Stands in for `@/lib/auth-storage` inside Storybook (aliased in main.ts).
 *
 * The real module reaches `aws-amplify/auth/cognito` for the token provider. That subpath cannot
 * simply be mocked alongside `aws-amplify/auth`: a string alias matches by prefix, so the existing
 * entry would rewrite it to `<mock>/cognito` and fail to resolve. Aliasing our own module instead
 * — the pattern already used for `@/lib/ketcher` — keeps the whole of Amplify out of the story.
 */
export function isRemembered() {
  return false;
}

export function setSessionPersistence() {}

export function applyStoredSessionPersistence() {}

export function clearStoredTokens() {}
