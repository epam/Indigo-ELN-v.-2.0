import { useState } from 'react';
import { expect, fn, screen, userEvent, waitFor, within } from 'storybook/test';

import { SolubilityDialog } from '@/components/experiments/stoichiometry/batches/solubility-dialog';
import { DICTIONARIES } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { SolubidityInSolvent } from '@/lib/types/reactions.ts';

const [, WATER, ETHANOL] = DICTIONARIES.SOLVENT!;

const VALUE = 'Solubility Value, g/ml';

function SolubilityDialogHarness({
  value = [],
  onSave,
}: {
  value?: SolubidityInSolvent[];
  onSave: (next: SolubidityInSolvent[]) => Promise<boolean>;
}) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <SolubilityDialog open={open} onOpenChange={setOpen} value={value} onSave={onSave} />
    </>
  );
}

const meta = {
  title: 'Experiments/Stoichiometry/Batches/SolubilityDialog',
  component: SolubilityDialogHarness,
  args: { onSave: fn(async () => true) },
} satisfies Meta<typeof SolubilityDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

const SAVED: SolubidityInSolvent[] = [
  { type: 'QUANTITATIVE', solvent: WATER, operator: 'LESS_THAN', value: 5, unit: 'G_ML' },
  { type: 'QUALITATIVE', solvent: ETHANOL, qualitativeType: 'SOLUBLE' },
];

/** A new row is quantitative, with `>` already chosen. */
export const Empty: Story = {
  play: async () => {
    await expect(await screen.findByLabelText('Solubility Type, row 1')).toHaveTextContent('Quantitative');
    await expect(screen.getByRole('button', { name: `${VALUE}, row 1 operator` })).toHaveTextContent('>');
  },
};

/** One row of each type: the value column is a number with an operator, or a verdict. */
export const Prefilled: Story = {
  args: { value: SAVED },
  play: async () => {
    await expect(await screen.findByRole('spinbutton', { name: `${VALUE}, row 1` })).toHaveValue(5);
    await expect(screen.getByRole('button', { name: `${VALUE}, row 1 operator` })).toHaveTextContent('<');
    await expect(screen.getByLabelText('Solubility Type, row 2')).toHaveTextContent('Qualitative');
    await expect(screen.getByLabelText(`${VALUE}, row 2`)).toHaveTextContent('Soluble');
  },
};

/** The type decides what the value column is, and only that type's fields are sent. */
export const SwitchingTypeSwapsTheValueControl: Story = {
  args: { value: SAVED.slice(0, 1) },
  play: async ({ args }) => {
    await userEvent.click(await screen.findByLabelText('Solubility Type, row 1'));
    await userEvent.click(await screen.findByRole('option', { name: 'Qualitative' }));

    await waitFor(() => expect(screen.queryByRole('spinbutton')).not.toBeInTheDocument());
    await userEvent.click(screen.getByLabelText(`${VALUE}, row 1`));
    await userEvent.click(await screen.findByRole('option', { name: 'Insoluble' }));

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() =>
      expect(args.onSave).toHaveBeenCalledWith([{ type: 'QUALITATIVE', solvent: WATER, qualitativeType: 'UNSOLUBLE' }]),
    );
  },
};

export const SavesAQuantitativeRow: Story = {
  play: async ({ args, canvasElement }) => {
    // A value with no solvent to attach it to holds Save back.
    await userEvent.type(await screen.findByRole('spinbutton', { name: `${VALUE}, row 1` }), '0.5');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled());

    await userEvent.click(screen.getByLabelText('Solvent Name, row 1'));
    await userEvent.click(await screen.findByRole('option', { name: 'Ethanol' }));
    await userEvent.click(screen.getByRole('button', { name: `${VALUE}, row 1 operator` }));
    await userEvent.click(await screen.findByRole('menuitem', { name: '≈' }));
    await userEvent.type(screen.getByLabelText('Comment, row 1'), 'warm');

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() =>
      expect(args.onSave).toHaveBeenCalledWith([
        {
          type: 'QUANTITATIVE',
          solvent: ETHANOL,
          operator: 'APPROXIMATELY',
          value: 0.5,
          unit: 'G_ML',
          comment: 'warm',
        },
      ]),
    );
    await expect(await within(canvasElement).findByText('Dialog closed.')).toBeInTheDocument();
  },
};

export const AddsAndRemovesRows: Story = {
  args: { value: SAVED },
  play: async ({ args }) => {
    await userEvent.click(await screen.findByRole('button', { name: 'Add Solubility' }));
    await expect(await screen.findByLabelText('Solvent Name, row 3')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Delete row 1' }));
    await waitFor(() => expect(screen.getByLabelText('Solvent Name, row 1')).toHaveTextContent('Ethanol'));

    // The added row was never filled in, so it is not sent.
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(args.onSave).toHaveBeenCalledWith(SAVED.slice(1)));
  },
};
