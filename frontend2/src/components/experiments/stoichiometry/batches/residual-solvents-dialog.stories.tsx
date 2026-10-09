import { useState } from 'react';
import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { ResidualSolventsDialog } from '@/components/experiments/stoichiometry/batches/residual-solvents-dialog';
import { DICTIONARIES } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ResidualSolvent } from '@/lib/types/reactions.ts';

const [TOLUENE, WATER] = DICTIONARIES.SOLVENT!;

function ResidualSolventsDialogHarness({
  value = [],
  onSave,
}: {
  value?: ResidualSolvent[];
  onSave: (next: ResidualSolvent[]) => Promise<boolean>;
}) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <ResidualSolventsDialog open={open} onOpenChange={setOpen} value={value} onSave={onSave} />
    </>
  );
}

const meta = {
  title: 'Experiments/Stoichiometry/Batches/ResidualSolventsDialog',
  component: ResidualSolventsDialogHarness,
  args: { onSave: fn(async () => true) },
} satisfies Meta<typeof ResidualSolventsDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

const SAVED: ResidualSolvent[] = [
  { solvent: TOLUENE, eq: 1.2, comment: 'trace' },
  { solvent: WATER, eq: 0.5 },
];

/** An empty list opens on one blank row rather than on nothing but the Add button. */
export const Empty: Story = {
  play: async () => {
    await expect(await screen.findByLabelText('Solvent Name, row 1')).toHaveTextContent('Select');
    await expect(screen.queryByLabelText('Solvent Name, row 2')).not.toBeInTheDocument();
  },
};

export const Prefilled: Story = {
  args: { value: SAVED },
  play: async () => {
    await expect(await screen.findByLabelText('Solvent Name, row 1')).toHaveTextContent('Toluene');
    await expect(screen.getByLabelText('# EQ. of Solvent, row 1')).toHaveValue(1.2);
    await expect(screen.getByLabelText('Comment, row 1')).toHaveValue('trace');
    await expect(screen.getByLabelText('Solvent Name, row 2')).toHaveTextContent('Water');
  },
};

/** Solvent and EQ are both `@NotNull`, so a row with only one of them holds Save back. */
export const SavesARow: Story = {
  play: async ({ args, canvasElement }) => {
    await userEvent.click(await screen.findByLabelText('Solvent Name, row 1'));
    await userEvent.click(await screen.findByRole('option', { name: 'Toluene' }));
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled());

    await userEvent.type(screen.getByLabelText('# EQ. of Solvent, row 1'), '1.5');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());

    // A second row left untouched is not an entry.
    await userEvent.click(screen.getByRole('button', { name: 'Add Solvent' }));
    await expect(await screen.findByLabelText('Solvent Name, row 2')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith([{ solvent: TOLUENE, eq: 1.5 }]));
    await expect(await within(canvasElement).findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/** Removing a row from the middle leaves the rest holding their own values. */
export const RemovesARow: Story = {
  args: { value: SAVED },
  play: async ({ args }) => {
    await userEvent.click(await screen.findByRole('button', { name: 'Delete row 1' }));
    await waitFor(() => expect(screen.getByLabelText('Solvent Name, row 1')).toHaveTextContent('Water'));
    await expect(screen.getByLabelText('# EQ. of Solvent, row 1')).toHaveValue(0.5);

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith([{ solvent: WATER, eq: 0.5 }]));
  },
};

/** The list mutation is `@NotNull`: no rows left is `[]`. */
export const RemovingEveryRowSavesEmpty: Story = {
  args: { value: SAVED.slice(0, 1) },
  play: async ({ args }) => {
    await userEvent.click(await screen.findByRole('button', { name: 'Delete row 1' }));
    await waitFor(() => expect(screen.queryByLabelText('Solvent Name, row 1')).not.toBeInTheDocument());

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith([]));
  },
};

/** A failed save has been toasted by `apiFetch`; the dialog keeps its rows. */
export const StaysOpenOnFailure: Story = {
  args: { value: SAVED, onSave: fn(async () => false) },
  play: async ({ args }) => {
    await userEvent.type(await screen.findByLabelText('Comment, row 2'), 'wet');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(args.onSave).toHaveBeenCalled());
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
    await expect(screen.getByLabelText('Comment, row 2')).toHaveValue('wet');
  },
};
