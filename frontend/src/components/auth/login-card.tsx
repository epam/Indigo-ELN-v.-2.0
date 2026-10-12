import { useForm } from '@tanstack/react-form';
import { confirmSignIn, signIn } from 'aws-amplify/auth';
import { CircleAlert, X } from 'lucide-react';
import { useState } from 'react';

import logoUrl from '@/assets/indigo-logo.svg';
import {
  confirmPasswordError,
  EMPTY_LOGIN_FORM,
  describeSignInError,
  UNSUPPORTED_CHALLENGE_MESSAGE,
  type LoginStep,
} from '@/components/auth/login-form';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Field } from '@/components/ui/field';
import { GroupInput, InputAction, InputGroup } from '@/components/ui/input';
import { PasswordInput } from '@/components/ui/password-input';
import { isRemembered, setSessionPersistence } from '@/lib/auth-storage';

import type { SignInOutput } from 'aws-amplify/auth';

/**
 * The sign-in screen, replacing Amplify's `<Authenticator>`.
 *
 * It talks to Cognito through Amplify's headless `aws-amplify/auth` API — the same SRP flow the
 * `<Authenticator>` ran, minus its UI and its global stylesheet. Nothing here reaches the backend:
 * the Quarkus side is a pure resource server and has no login endpoint.
 *
 * Two steps share one frame. `CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED` is not optional to
 * handle: `CognitoStack.java` seeds `admin` in FORCE_CHANGE_PASSWORD, so a card that only knew
 * `DONE` would lock that user out of the app entirely.
 */
function LoginCard({ onSignedIn }: { onSignedIn: () => void }) {
  const [step, setStep] = useState<LoginStep>('credentials');
  const [error, setError] = useState<string>();

  const form = useForm({
    defaultValues: { ...EMPTY_LOGIN_FORM, rememberMe: isRemembered() },
    onSubmit: async ({ value }) => {
      setError(undefined);

      /** Both calls answer the same shape, and both can end on a challenge rather than a session. */
      const handle = ({ isSignedIn, nextStep }: SignInOutput) => {
        if (isSignedIn) return onSignedIn();
        if (nextStep.signInStep === 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED') return setStep('newPassword');
        // MFA, TOTP setup, RESET_PASSWORD — real steps with no screen behind them.
        setError(UNSUPPORTED_CHALLENGE_MESSAGE);
      };

      try {
        if (step === 'credentials') {
          const username = value.username.trim();
          if (!username || !value.password) {
            // The banner is the card's only error surface, so an empty field speaks through it
            // rather than through a second style of message under the control.
            setError('Enter your Username and Password.');
            return;
          }
          // Chosen before signIn, so the tokens it issues land in the store the checkbox asked for.
          setSessionPersistence(value.rememberMe);
          handle(await signIn({ username, password: value.password }));
          return;
        }

        const mismatch = confirmPasswordError(value.newPassword, value.confirmPassword);
        if (mismatch) {
          setError(mismatch);
          return;
        }
        handle(await confirmSignIn({ challengeResponse: value.newPassword }));
      } catch (caught) {
        // A session already exists in this tab — nothing failed, there is just nothing to do.
        if ((caught as { name?: string }).name === 'UserAlreadyAuthenticatedException') {
          onSignedIn();
          return;
        }
        setError(describeSignInError(caught));
      }
    },
  });

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-8 bg-blue-500 p-6">
      <img src={logoUrl} alt="Indigo ELN" width={152} height={45} className="h-[45px] w-[152px]" />

      <form
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          void form.handleSubmit();
        }}
        // The dividers are inset to the content width, so they belong to the rows rather than to
        // the card: the horizontal padding lives here and the rows carry only their own border.
        className="w-full max-w-125 rounded-lg bg-card px-6 shadow-card"
      >
        <h1 className="border-b border-neutral-300 py-6 text-[20px]/6 font-semibold text-neutral-1000">
          {step === 'credentials' ? 'Log in' : 'Set a new password'}
        </h1>

        {/* Scrolls rather than clipping the footer button on a short window. */}
        <div className="flex max-h-[calc(100svh-20rem)] flex-col gap-5 overflow-y-auto py-6">
          {step === 'credentials' ? (
            <>
              <form.Field name="username">
                {(field) => (
                  <Field id={field.name} label="Username">
                    <InputGroup className="h-11">
                      <GroupInput
                        id={field.name}
                        name={field.name}
                        autoComplete="username"
                        placeholder="Enter your Username"
                        value={field.state.value}
                        onBlur={field.handleBlur}
                        onChange={(event) => field.handleChange(event.target.value)}
                      />
                      {field.state.value && (
                        <InputAction label="Clear Username" onClick={() => field.handleChange('')}>
                          <X className="size-4" />
                        </InputAction>
                      )}
                    </InputGroup>
                  </Field>
                )}
              </form.Field>

              <form.Field name="password">
                {(field) => (
                  <Field id={field.name} label="Password">
                    <PasswordInput
                      className="h-11"
                      id={field.name}
                      label="Password"
                      autoComplete="current-password"
                      placeholder="Enter your Password"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={field.handleChange}
                    />
                  </Field>
                )}
              </form.Field>

              <form.Field name="rememberMe">
                {(field) => (
                  <label className="flex w-fit cursor-pointer items-center gap-2 text-[14px]/6 text-neutral-1000">
                    <Checkbox checked={field.state.value} onCheckedChange={(checked) => field.handleChange(checked)} />
                    Remember me
                  </label>
                )}
              </form.Field>
            </>
          ) : (
            <>
              <form.Field name="newPassword">
                {(field) => (
                  <Field id={field.name} label="New Password">
                    <PasswordInput
                      className="h-11"
                      id={field.name}
                      label="New Password"
                      autoComplete="new-password"
                      placeholder="Enter your new Password"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={field.handleChange}
                    />
                  </Field>
                )}
              </form.Field>

              <form.Field name="confirmPassword">
                {(field) => (
                  <Field id={field.name} label="Confirm Password">
                    <PasswordInput
                      className="h-11"
                      id={field.name}
                      label="Confirm Password"
                      autoComplete="new-password"
                      placeholder="Repeat your new Password"
                      value={field.state.value}
                      onBlur={field.handleBlur}
                      onChange={field.handleChange}
                    />
                  </Field>
                )}
              </form.Field>
            </>
          )}

          {error && (
            <div
              role="alert"
              className="flex items-center gap-2 rounded-md border border-red-200 bg-red-10 px-3 py-3 text-[14px]/6 text-neutral-1000"
            >
              <CircleAlert aria-hidden className="size-5 shrink-0 text-red-200" />
              {error}
            </div>
          )}
        </div>

        <div className="border-t border-neutral-300 py-4">
          <form.Subscribe selector={(state) => state.isSubmitting}>
            {(isSubmitting) => (
              <Button type="submit" loading={isSubmitting} className="h-12 w-full text-[16px]">
                {step === 'credentials' ? 'Log in' : 'Save and log in'}
              </Button>
            )}
          </form.Subscribe>
        </div>
      </form>
    </div>
  );
}

export { LoginCard };
