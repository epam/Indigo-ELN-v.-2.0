import { useState } from 'react';
import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { MeltingPointDialog } from '@/components/experiments/stoichiometry/batches/melting-point-dialog';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { MeltingPoint } from '@/lib/types/reactions.ts';

function MeltingPointDialogHarness({
  value,
  onSave,
}: {
  value?: MeltingPoint;
  onSave: (next: MeltingPoint | null) => Promise<boolean>;
}) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <MeltingPointDialog open={open} onOpenChange={setOpen} value={value} onSave={onSave} />
    </>
  );
}

const meta = {
  title: 'Experiments/Stoichiometry/Batches/MeltingPointDialog',
  component: MeltingPointDialogHarness,
  args: { onSave: fn(async () => true) },
} satisfies Meta<typeof MeltingPointDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Nothing recorded yet. */
export const Empty: Story = {};

export const Prefilled: Story = {
  args: { value: { lower: 67, upper: 69, comments: 'decomposes' } },
  play: async () => {
    await expect(await screen.findByLabelText('Lower, °C')).toHaveValue(67);
    await expect(screen.getByLabelText('Upper, °C')).toHaveValue(69);
    await expect(screen.getByLabelText('Comments')).toHaveValue('decomposes');
  },
};

export const SavesTheRange: Story = {
  play: async ({ args, canvasElement }) => {
    await userEvent.type(await screen.findByLabelText('Lower, °C'), '67.5');
    await userEvent.type(screen.getByLabelText('Upper, °C'), '69');
    await userEvent.type(screen.getByLabelText('Comments'), 'decomposes');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith({ lower: 67.5, upper: 69, comments: 'decomposes' }));
    await expect(await within(canvasElement).findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/** `SetOutputMeltingPoint` takes `null` for "no melting point", not an object of blanks. */
export const EmptiedSavesNull: Story = {
  args: { value: { lower: 67 } },
  play: async ({ args }) => {
    await userEvent.clear(await screen.findByLabelText('Lower, °C'));
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith(null));
  },
};

/** **Regression guard:** Save on an untouched dialog closes it and sends nothing. */
export const UntouchedSaveSendsNothing: Story = {
  args: { value: { lower: 67, upper: 69 } },
  play: async ({ args, canvasElement }) => {
    await userEvent.click(await screen.findByRole('button', { name: 'Save' }));

    await expect(await within(canvasElement).findByText('Dialog closed.')).toBeInTheDocument();
    await expect(args.onSave).not.toHaveBeenCalled();
  },
};

/** A failed save has been toasted by `apiFetch`; the dialog keeps what was typed. */
export const StaysOpenOnFailure: Story = {
  args: { onSave: fn(async () => false) },
  play: async ({ args }) => {
    await userEvent.type(await screen.findByLabelText('Lower, °C'), '67');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(args.onSave).toHaveBeenCalled());
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
    await expect(screen.getByLabelText('Lower, °C')).toHaveValue(67);
  },
};
