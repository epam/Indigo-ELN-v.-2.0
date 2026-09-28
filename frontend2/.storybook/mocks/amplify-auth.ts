/**
 * Stands in for `aws-amplify/auth` inside Storybook (aliased in main.ts).
 * Amplify is never configured there, so the real module would throw.
 */
import type { ConfirmSignInOutput, SignInOutput } from 'aws-amplify/auth';

export async function fetchAuthSession() {
  return { tokens: { accessToken: { toString: () => 'storybook-token' } } };
}

export async function signOut() {}

const SIGNED_IN: SignInOutput = { isSignedIn: true, nextStep: { signInStep: 'DONE' } };

let signInImpl: () => Promise<SignInOutput> = async () => SIGNED_IN;
let confirmSignInImpl: () => Promise<ConfirmSignInOutput> = async () => SIGNED_IN;

export async function signIn(): Promise<SignInOutput> {
  return signInImpl();
}

export async function confirmSignIn(): Promise<ConfirmSignInOutput> {
  return confirmSignInImpl();
}

/**
 * Chooses what the next sign-in does. A story sets it in a decorator or a `play`; passing nothing
 * puts both calls back to succeeding, so one story cannot leak its outcome into the next.
 */
export function __setAuthBehavior(next?: {
  signIn?: () => Promise<SignInOutput>;
  confirmSignIn?: () => Promise<ConfirmSignInOutput>;
}) {
  signInImpl = next?.signIn ?? (async () => SIGNED_IN);
  confirmSignInImpl = next?.confirmSignIn ?? (async () => SIGNED_IN);
}
