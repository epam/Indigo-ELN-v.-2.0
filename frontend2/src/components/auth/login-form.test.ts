import { describe, expect, it } from 'vitest';

import { confirmPasswordError, describeSignInError } from '@/components/auth/login-form';

const cognitoError = (name: string, message = 'raw cognito text') => Object.assign(new Error(message), { name });

describe('describeSignInError', () => {
  it('says the same thing for a bad password as for a username that does not exist', () => {
    // Anything else would turn the form into a way of discovering which usernames are real.
    const wrongPassword = describeSignInError(cognitoError('NotAuthorizedException'));
    expect(wrongPassword).toBe('Incorrect username or password.');
    expect(describeSignInError(cognitoError('UserNotFoundException'))).toBe(wrongPassword);
  });

  it('passes a password-policy rejection through, because it carries the pool rules', () => {
    const message = 'Password did not conform with policy: Password not long enough';
    expect(describeSignInError(cognitoError('InvalidPasswordException', message))).toBe(message);
  });

  it('names the administrator for the states a user cannot resolve here', () => {
    expect(describeSignInError(cognitoError('PasswordResetRequiredException'))).toMatch(/administrator/);
    expect(describeSignInError(cognitoError('UserNotConfirmedException'))).toMatch(/administrator/);
  });

  it('asks the user to wait when Cognito is throttling', () => {
    expect(describeSignInError(cognitoError('TooManyRequestsException'))).toMatch(/try again/i);
  });

  it('falls back to the error message, then to generic copy', () => {
    expect(describeSignInError(cognitoError('SomethingNew', 'network unreachable'))).toBe('network unreachable');
    expect(describeSignInError({})).toBe('Could not log in. Try again.');
  });
});

describe('confirmPasswordError', () => {
  it('reports an empty confirmation and a mismatch differently', () => {
    expect(confirmPasswordError('secret', '')).toBe('Confirm your new password');
    expect(confirmPasswordError('secret', 'secrat')).toBe('Passwords do not match');
  });

  it('passes a matching pair', () => {
    expect(confirmPasswordError('secret', 'secret')).toBeUndefined();
  });
});
