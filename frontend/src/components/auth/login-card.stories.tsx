import { expect, fn, userEvent, within } from 'storybook/test';

import { __setAuthBehavior } from '../../../.storybook/mocks/amplify-auth';
import { LoginCard } from '@/components/auth/login-card';

import type { Meta, StoryObj } from '@storybook/react-vite';

const cognitoError = (name: string, message: string) => Object.assign(new Error(message), { name });

/**
 * Cognito is reached through the `aws-amplify/auth` mock that `.storybook/main.ts` aliases in, so
 * these stories choose an outcome rather than a network response. The meta-level `beforeEach`
 * resets it first, which is why a story that sets nothing gets a plain successful sign-in.
 */
const meta = {
  title: 'Auth/LoginCard',
  component: LoginCard,
  parameters: { layout: 'fullscreen' },
  args: { onSignedIn: fn() },
  beforeEach: () => {
    __setAuthBehavior();
  },
} satisfies Meta<typeof LoginCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The design's filled state: both clear buttons showing, the password revealed. */
export const Filled: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Username'), 'jsmith');
    await userEvent.type(canvas.getByLabelText('Password'), 'hunter2hunter2');
    await userEvent.click(canvas.getByRole('button', { name: 'Show Password' }));
    await expect(canvas.getByLabelText('Password')).toHaveAttribute('type', 'text');
    await expect(canvas.getByRole('button', { name: 'Clear Username' })).toBeVisible();
  },
};

/** Cognito's own wording, in the only error surface the card has. */
export const IncorrectPassword: Story = {
  beforeEach: () => {
    __setAuthBehavior({
      signIn: () => Promise.reject(cognitoError('NotAuthorizedException', 'Incorrect username or password.')),
    });
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Username'), 'jsmith');
    await userEvent.type(canvas.getByLabelText('Password'), 'wrong');
    await userEvent.click(canvas.getByRole('button', { name: 'Log in' }));
    await expect(await canvas.findByRole('alert')).toHaveTextContent('Incorrect username or password.');
  },
};

/** Submitting with nothing typed is answered in the banner, not under the fields. */
export const MissingCredentials: Story = {
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Log in' }));
    await expect(await canvas.findByRole('alert')).toHaveTextContent('Enter your Username and Password.');
    await expect(args.onSignedIn).not.toHaveBeenCalled();
  },
};

/** The seeded `admin` user is in FORCE_CHANGE_PASSWORD, so this step is not optional. */
export const NewPasswordRequired: Story = {
  beforeEach: () => {
    __setAuthBehavior({
      signIn: async () => ({
        isSignedIn: false,
        nextStep: { signInStep: 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED' },
      }),
    });
  },
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Username'), 'admin');
    await userEvent.type(canvas.getByLabelText('Password'), 'Temp0rary!');
    await userEvent.click(canvas.getByRole('button', { name: 'Log in' }));

    await userEvent.type(await canvas.findByLabelText('New Password'), 'Str0ngEnough!');
    await userEvent.type(canvas.getByLabelText('Confirm Password'), 'Str0ngEnough?');
    await userEvent.click(canvas.getByRole('button', { name: 'Save and log in' }));
    await expect(await canvas.findByRole('alert')).toHaveTextContent('Passwords do not match');

    await userEvent.clear(canvas.getByLabelText('Confirm Password'));
    await userEvent.type(canvas.getByLabelText('Confirm Password'), 'Str0ngEnough!');
    await userEvent.click(canvas.getByRole('button', { name: 'Save and log in' }));
    await expect(args.onSignedIn).toHaveBeenCalled();
  },
};

/** A challenge with no screen behind it — MFA, a TOTP setup — says so rather than hanging. */
export const UnsupportedChallenge: Story = {
  beforeEach: () => {
    __setAuthBehavior({
      signIn: async () => ({ isSignedIn: false, nextStep: { signInStep: 'CONFIRM_SIGN_IN_WITH_SMS_CODE' } }),
    });
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Username'), 'jsmith');
    await userEvent.type(canvas.getByLabelText('Password'), 'hunter2hunter2');
    await userEvent.click(canvas.getByRole('button', { name: 'Log in' }));
    await expect(await canvas.findByRole('alert')).toHaveTextContent(/not supported here/);
  },
};

/** The button's spinner, held open by a sign-in that never resolves. */
export const Submitting: Story = {
  beforeEach: () => {
    __setAuthBehavior({ signIn: () => new Promise(() => {}) });
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Username'), 'jsmith');
    await userEvent.type(canvas.getByLabelText('Password'), 'hunter2hunter2');
    await userEvent.click(canvas.getByRole('button', { name: 'Log in' }));
    await expect(canvas.getByRole('button', { name: 'Log in' })).toHaveAttribute('aria-busy', 'true');
  },
};
