import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { Button } from '@/components/ui/button';
import { ApiError } from '@/lib/api';
import { notifyError, toastManager } from '@/lib/toast';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** The provider itself comes from the global withToast decorator. */
function ToastHarness() {
  return (
    <div className="flex gap-3">
      <Button onClick={() => notifyError(new ApiError(403, null), '/api/eln/projects')}>Forbidden</Button>
      <Button onClick={() => notifyError(new ApiError(400, [{ path: 'name', message: 'must not be empty' }]))}>
        Validation
      </Button>
      <Button onClick={() => notifyError(new ApiError(500, null))}>Server error</Button>
      <Button onClick={() => notifyError(new ApiError(401, { message: 'Unauthorized' }), '/api/eln/projects')}>
        Expired token
      </Button>
      <Button onClick={() => notifyError(new ApiError(401, 'Invalid API secret'), '/api/eln/projects')}>
        Proxy misconfigured
      </Button>
      <Button variant="secondary" onClick={() => toastManager.add({ title: 'Project created' })}>
        Plain
      </Button>
    </div>
  );
}

const meta = {
  title: 'UI/Toast',
  component: ToastHarness,
} satisfies Meta<typeof ToastHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** A 403 is reported as a permission problem rather than a raw status. */
export const ForbiddenMessage: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Forbidden' }));
    await waitFor(() =>
      expect(screen.getByText("You don't have permission to perform this action")).toBeInTheDocument(),
    );
  },
};

/** Bean-validation bodies are listed one field per line. */
export const ValidationMessage: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Validation' }));
    await waitFor(() => expect(screen.getByText('name: must not be empty')).toBeInTheDocument());
  },
};

export const Dismissable: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Server error' }));
    await waitFor(() => expect(screen.getByText('Server error. Please try again later')).toBeInTheDocument());

    // Queried by label, not by role: Base UI marks an unfocused high-priority toast
    // aria-hidden and announces it through a visually-hidden role="alert" region
    // instead, so its controls are deliberately outside the accessibility tree until
    // the viewport takes focus (F6).
    await userEvent.click(screen.getByLabelText('Dismiss'));
    await waitFor(() => expect(screen.queryByText('Server error. Please try again later')).not.toBeInTheDocument());
  },
};

/**
 * A 401 reads as one authorization failure carrying whatever the backend said, and offers
 * a reload — which re-runs the `_auth` guard and so lands an ended session on /login.
 */
export const AuthorizationFailed: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Expired token' }));
    await waitFor(() => expect(screen.getByText('Authorization failed')).toBeInTheDocument());

    // The detail is what separates an expired token from a misconfigured proxy.
    expect(screen.getByText('Unauthorized')).toBeInTheDocument();
    // Present, but deliberately not clicked: it reloads the page the tests run in.
    expect(screen.getByText('Reload')).toBeInTheDocument();
  },
};

/** The other 401: APISecretFilter, i.e. the request never went through CloudFront. */
export const AuthorizationFailedDetail: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Proxy misconfigured' }));
    await waitFor(() => expect(screen.getByText('Invalid API secret')).toBeInTheDocument());
  },
};

/**
 * A screenful of parallel queries all answer 401 at once. The fixed toast id is what
 * collapses that burst into the single problem it actually is.
 */
export const AuthorizationFailedIsDeduped: Story = {
  play: async () => {
    const trigger = screen.getByRole('button', { name: 'Expired token' });
    await userEvent.click(trigger);
    await userEvent.click(trigger);
    await userEvent.click(trigger);

    await waitFor(() => expect(screen.getByText('Authorization failed')).toBeInTheDocument());
    // One toast, so one dismiss control — not three stacked copies of the same message.
    expect(screen.getAllByText('Authorization failed')).toHaveLength(1);
    expect(screen.getAllByLabelText('Dismiss')).toHaveLength(1);
  },
};
