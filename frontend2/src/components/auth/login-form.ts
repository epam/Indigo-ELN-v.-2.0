import { z } from '@/lib/zod';

/** The two steps the card can be on: the credentials it starts with, and Cognito's challenge. */
export type LoginStep = 'credentials' | 'newPassword';

export interface LoginFormValues {
  username: string;
  password: string;
  rememberMe: boolean;
  newPassword: string;
  confirmPassword: string;
}

export const EMPTY_LOGIN_FORM: LoginFormValues = {
  username: '',
  password: '',
  rememberMe: false,
  newPassword: '',
  confirmPassword: '',
};

export const usernameSchema = z.string().trim().min(1, 'Enter your Username');
export const passwordSchema = z.string().min(1, 'Enter your Password');

/**
 * The new password is only checked for being present and for matching its confirmation.
 *
 * The password *policy* deliberately is not repeated here: it lives in `CognitoStack.java`
 * (minLength 6, no character-class rules) and Cognito enforces it itself, answering
 * `InvalidPasswordException` with a message that states the real rules. indigo-frontend's
 * `main.ts` advertises a stricter policy than the pool actually has, which is the mistake this
 * avoids — a client-side copy can only ever drift from the one that is enforced.
 */
export const newPasswordSchema = z.string().min(1, 'Enter a new password');

export function confirmPasswordError(newPassword: string, confirmPassword: string): string | undefined {
  if (confirmPassword.length === 0) return 'Confirm your new password';
  return confirmPassword === newPassword ? undefined : 'Passwords do not match';
}

/** Shown when Cognito asks for something this screen has no step for — MFA, TOTP setup, a reset. */
export const UNSUPPORTED_CHALLENGE_MESSAGE =
  'This account requires an additional verification step that is not supported here. Contact an administrator.';

const GENERIC_MESSAGE = 'Could not log in. Try again.';

/**
 * Cognito's error names turned into the copy the card shows.
 *
 * `NotAuthorizedException` and `UserNotFoundException` deliberately produce the *same* string, so
 * the form cannot be used to discover which usernames exist. It is also Cognito's own wording for
 * the first of the two, and what the design shows.
 */
export function describeSignInError(error: unknown): string {
  const { name, message } = error as { name?: string; message?: string };

  switch (name) {
    case 'NotAuthorizedException':
    case 'UserNotFoundException':
      return 'Incorrect username or password.';
    case 'PasswordResetRequiredException':
      return 'Your password must be reset before you can log in. Contact an administrator.';
    case 'UserNotConfirmedException':
      return 'This account has not been confirmed yet. Contact an administrator.';
    // Both carry the pool's own policy in their message, which is more use than anything generic.
    case 'InvalidPasswordException':
    case 'InvalidParameterException':
      return message || GENERIC_MESSAGE;
    case 'TooManyRequestsException':
    case 'LimitExceededException':
      return 'Too many attempts. Wait a moment and try again.';
    default:
      return message || GENERIC_MESSAGE;
  }
}
