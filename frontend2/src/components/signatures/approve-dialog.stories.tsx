import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { ApproveDialog } from '@/components/signatures/approve-dialog';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Signatures/ApproveDialog',
  component: ApproveDialog,
  args: { open: true, onOpenChange: fn(), onSubmit: fn(async () => {}) },
} satisfies Meta<typeof ApproveDialog>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Nothing chosen yet, so Approve is disabled. */
export const Empty: Story = {
  play: async () => {
    const dialog = within(await screen.findByRole('dialog'));
    await expect(dialog.getByRole('button', { name: 'Approve' })).toBeDisabled();
  },
};

/** The chosen file's name replaces the prompt, and both values reach the caller. */
export const Submits: Story = {
  play: async ({ args }) => {
    const dialog = within(await screen.findByRole('dialog'));
    const keystore = new File(['key'], 'john.p12');
    await userEvent.upload(dialog.getByLabelText(/Keystore File/), keystore);
    await expect(dialog.getByText('john.p12')).toBeVisible();
    await userEvent.type(dialog.getByLabelText('Keystore Password'), 'secret');
    await userEvent.click(dialog.getByRole('button', { name: 'Approve' }));
    await waitFor(() => expect(args.onSubmit).toHaveBeenCalledWith(keystore, 'secret'));
  },
};

/** A keystore with an empty password is still a keystore. */
export const EmptyPassword: Story = {
  play: async ({ args }) => {
    const dialog = within(await screen.findByRole('dialog'));
    const keystore = new File(['key'], 'john.p12');
    await userEvent.upload(dialog.getByLabelText(/Keystore File/), keystore);
    await userEvent.click(dialog.getByRole('button', { name: 'Approve' }));
    await waitFor(() => expect(args.onSubmit).toHaveBeenCalledWith(keystore, ''));
  },
};
