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
