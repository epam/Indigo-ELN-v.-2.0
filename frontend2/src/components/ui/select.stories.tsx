import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { Select } from '@/components/ui/select';

import type { Meta, StoryObj } from '@storybook/react-vite';

const ROLES = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT'] as const;
type Role = (typeof ROLES)[number];

const LABELS: Record<Role, string> = {
  REACTANT: 'Reactant',
  REAGENT: 'Reagent',
  CATALYST: 'Catalyst',
  SOLVENT: 'Solvent',
};

function ControlledSelect({ disabled }: { disabled?: boolean }) {
  const [value, setValue] = useState<Role>('REACTANT');
  return (
    <div className="w-64">
      <Select<Role>
        aria-label="Rxn Role"
        value={value}
        onValueChange={(next) => next != null && setValue(next)}
        items={[...ROLES]}
        itemToKey={(role) => role}
        itemToLabel={(role) => LABELS[role]}
        disabled={disabled}
      />
    </div>
  );
}

const meta = {
  title: 'UI/Select',
  component: ControlledSelect,
} satisfies Meta<typeof ControlledSelect>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Closed, showing the current choice. */
export const Default: Story = {};

/**
 * **The reason this is not a `Combobox`.** The trigger is a button, so there is nothing to type
 * into and nothing to clear — the two affordances a combobox would have offered for a field that
 * accepts four exact values and will not accept none of them.
 */
export const HasNoTextInputAndNoClear: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('textbox')).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Clear selection' })).not.toBeInTheDocument();
    // Typing goes nowhere: the trigger holds a label, not an editable value.
    const trigger = canvas.getByLabelText('Rxn Role');
    await userEvent.type(trigger, 'xyz');
    await expect(trigger).toHaveTextContent('Reactant');
  },
};

/** Picking an option commits it and closes the list. */
export const PicksAnOption: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByLabelText('Rxn Role'));

    // The popup is portalled, so it is reached with `screen`.
    await userEvent.click(await screen.findByRole('option', { name: 'Solvent' }));
    await waitFor(() => expect(canvas.getByLabelText('Rxn Role')).toHaveTextContent('Solvent'));
  },
};

/** Fully operable from the keyboard, which is most of why this is a real select. */
export const KeyboardSelection: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const trigger = canvas.getByLabelText('Rxn Role');

    trigger.focus();
    await userEvent.keyboard('{Enter}');
    await expect(await screen.findByRole('listbox')).toBeInTheDocument();

    await userEvent.keyboard('{ArrowDown}{Enter}');
    await waitFor(() => expect(trigger).toHaveTextContent('Reagent'));
  },
};

/** A reader who cannot edit sees the choice and cannot open the list. */
export const Disabled: Story = {
  args: { disabled: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const trigger = canvas.getByLabelText('Rxn Role');
    await expect(trigger).toBeDisabled();
    await expect(trigger).toHaveTextContent('Reactant');
  },
};

/**
 * A required field starts empty. It leaves `emptyLabel` off — there must be no row that puts the
 * value back to null — so `placeholder` is the only thing naming the control until a choice is
 * made, and it goes once one is.
 */
export const Placeholder: Story = {
  render: () => {
    function EmptySelect() {
      const [value, setValue] = useState<Role | null>(null);
      return (
        <div className="w-64">
          <Select<Role>
            aria-label="Rxn Role"
            value={value}
            onValueChange={setValue}
            items={[...ROLES]}
            itemToKey={(role) => role}
            itemToLabel={(role) => LABELS[role]}
            placeholder="Select Rxn Role"
          />
        </div>
      );
    }
    return <EmptySelect />;
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const trigger = canvas.getByLabelText('Rxn Role');
    await expect(trigger).toHaveTextContent('Select Rxn Role');
    await userEvent.click(trigger);
    // No clear-row: the four values are the whole list.
    await expect(await screen.findByRole('option', { name: 'Reagent' })).toBeInTheDocument();
    await expect(screen.getAllByRole('option')).toHaveLength(ROLES.length);
    await userEvent.click(screen.getByRole('option', { name: 'Reagent' }));
    await waitFor(() => expect(trigger).toHaveTextContent('Reagent'));
  },
};
