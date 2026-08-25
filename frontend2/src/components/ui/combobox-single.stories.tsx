import { useState } from 'react';
import { expect, screen, userEvent, within } from 'storybook/test';

import { Combobox } from '@/components/ui/combobox';

import type { Meta, StoryObj } from '@storybook/react-vite';

interface Option {
  id: string;
  name: string;
}

const OPTIONS: Option[] = [
  { id: 'ta-1', name: 'Obesity' },
  { id: 'ta-2', name: 'Oncology' },
  { id: 'ta-3', name: 'Cardiology' },
  { id: 'ta-4', name: 'Immunology' },
  { id: 'ta-5', name: 'Neurology' },
];

/** Object items, as every real call site has — the filtering itself is Base UI's. */
function ComboboxHarness({ loading = false, error = false }: { loading?: boolean; error?: boolean }) {
  const [value, setValue] = useState<Option | null>(null);

  return (
    <div className="w-[420px]">
      <label htmlFor="therapeutic-area" className="text-[14px]/6">
        Therapeutic Area
      </label>
      <Combobox<Option>
        id="therapeutic-area"
        value={value}
        onValueChange={setValue}
        items={loading || error ? [] : OPTIONS}
        itemToKey={(item) => item.id}
        itemToLabel={(item) => item.name}
        loading={loading}
        error={error}
      />
    </div>
  );
}

const meta = {
  title: 'UI/Combobox',
  component: ComboboxHarness,
  args: {},
} satisfies Meta<typeof ComboboxHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The trigger opens the full list — this is a picker, not a search box. */
export const OpensTheWholeList: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Show options' }));
    await expect(await screen.findByRole('option', { name: 'Obesity' })).toBeInTheDocument();
    await expect(screen.getAllByRole('option')).toHaveLength(OPTIONS.length);
  },
};

/** Typing narrows the list locally, and choosing puts the label in the input. */
export const FiltersAndSelects: Story = {
  play: async ({ canvasElement }) => {
    const input = within(canvasElement).getByLabelText('Therapeutic Area');
    await userEvent.type(input, 'onc');
    await userEvent.click(await screen.findByRole('option', { name: 'Oncology' }));
    await expect(input).toHaveValue('Oncology');
  },
};

/** Every filter this backs is optional, so clearing has to be reachable. */
export const Clears: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText('Therapeutic Area');
    await userEvent.type(input, 'obe');
    await userEvent.click(await screen.findByRole('option', { name: 'Obesity' }));
    await expect(input).toHaveValue('Obesity');

    await userEvent.click(canvas.getByRole('button', { name: 'Clear selection' }));
    await expect(input).toHaveValue('');
  },
};

/** "No matches" is a claim about a finished load, so it waits for one. */
export const Loading: Story = {
  args: { loading: true },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Show options' }));
    await expect(await screen.findByText('Searching…')).toBeInTheDocument();
    await expect(screen.queryByText('No matches')).not.toBeInTheDocument();
  },
};

/** apiFetch toasts the failure; the popup only explains why it is empty. */
export const LookupFailed: Story = {
  args: { error: true },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Show options' }));
    await expect(await screen.findByText('Could not load options')).toBeInTheDocument();
  },
};
