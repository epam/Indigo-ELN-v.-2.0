import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { FormDialog } from '@/components/common/form-dialog';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';

import type { Meta, StoryObj } from '@storybook/react-vite';

function FormDialogHarness({
  outcome,
  initializing,
}: {
  outcome: 'success' | 'failure' | 'pending';
  initializing?: boolean;
}) {
  const [open, setOpen] = useState(true);

  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <FormDialog
        open={open}
        onOpenChange={setOpen}
        title="Add Project"
        initializing={initializing}
        onSubmit={async () => {
          await new Promise((resolve) => setTimeout(resolve, outcome === 'pending' ? 100_000 : 50));
          if (outcome === 'failure') throw new Error('Server error. Please try again later');
          setOpen(false);
        }}
      >
        <Field id="name" label="Project Name" required>
          <Input id="name" placeholder="Project Name" />
        </Field>
      </FormDialog>
    </>
  );
}

const meta = {
  title: 'Common/FormDialog',
  component: FormDialogHarness,
  args: { outcome: 'success' },
} satisfies Meta<typeof FormDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Save disables itself and shows a spinner while the submit is in flight. */
export const Submitting: Story = {
  args: { outcome: 'pending' },
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    // The label survives the spinner, so the button is still findable by its name.
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled());
    await expect(screen.getByRole('button', { name: 'Save' })).toHaveAttribute('aria-busy', 'true');
  },
};

/** A rejected submit clears the spinner and leaves the dialog open with its input intact. */
export const StaysOpenOnFailure: Story = {
  args: { outcome: 'failure' },
  play: async () => {
    await userEvent.type(screen.getByLabelText(/Project Name/), 'Kinase');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
    await expect(screen.getByRole('heading', { name: 'Add Project' })).toBeInTheDocument();
    await expect(screen.getByLabelText(/Project Name/)).toHaveValue('Kinase');
  },
};

/** A resolved submit is the caller's cue to close. */
export const ClosesOnSuccess: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(within(canvasElement).getByText('Dialog closed.')).toBeInTheDocument());
  },
};

/** Ctrl+Enter presses the default button from a field, without clicking Save. */
export const CtrlEnterSubmits: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(screen.getByLabelText(/Project Name/));
    await userEvent.keyboard('{Control>}{Enter}{/Control}');
    await waitFor(() => expect(within(canvasElement).getByText('Dialog closed.')).toBeInTheDocument());
  },
};

/** Cmd+Enter does the same, for Mac keyboards. */
export const MetaEnterSubmits: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(screen.getByLabelText(/Project Name/));
    await userEvent.keyboard('{Meta>}{Enter}{/Meta}');
    await waitFor(() => expect(within(canvasElement).getByText('Dialog closed.')).toBeInTheDocument());
  },
};

/** The shortcut is ignored while a submit is already in flight. */
export const CtrlEnterIgnoredWhileSubmitting: Story = {
  args: { outcome: 'pending' },
  play: async () => {
    await userEvent.click(screen.getByLabelText(/Project Name/));
    await userEvent.keyboard('{Control>}{Enter}{/Control}');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toHaveAttribute('aria-busy', 'true'));

    // A second press must not start a competing submit.
    await userEvent.keyboard('{Control>}{Enter}{/Control}');
    await expect(screen.getByRole('button', { name: 'Save' })).toHaveAttribute('aria-busy', 'true');
  },
};

/**
 * The initializing phase: the body inert under a spinner, Save disabled. The spinner is asserted
 * without waiting anything out, which is what pins the `showDelayMs={0}` override — `SavingOverlay`
 * otherwise suppresses it for 300 ms, which is right for a blur-to-save and wrong for a form that
 * has nothing in it yet.
 */
export const Initializing: Story = {
  args: { initializing: true },
  play: async () => {
    await expect(await screen.findByRole('status')).toHaveTextContent('Loading…');
    await expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();
  },
};

/** Nothing has been typed yet, so unlike the submitting phase this one does not trap the user. */
export const InitializingStillCloses: Story = {
  args: { initializing: true },
  play: async () => {
    await userEvent.click(await screen.findByRole('button', { name: 'Cancel' }));
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};
