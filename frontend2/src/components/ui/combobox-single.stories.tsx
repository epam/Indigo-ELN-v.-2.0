import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { SavingOverlay } from '@/components/common/saving-overlay';
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
function ComboboxHarness({
  initial = null,
  loading = false,
  error = false,
  disabled = false,
  saving = false,
}: {
  initial?: Option | null;
  loading?: boolean;
  error?: boolean;
  disabled?: boolean;
  /** Wraps the control the way a blur-saved form field does — see the `Saving` story. */
  saving?: boolean;
}) {
  const [value, setValue] = useState<Option | null>(initial);

  const control = (
    <Combobox<Option>
      id="therapeutic-area"
      value={value}
      onValueChange={setValue}
      items={loading || error ? [] : OPTIONS}
      itemToKey={(item) => item.id}
      itemToLabel={(item) => item.name}
      loading={loading}
      error={error}
      disabled={disabled}
    />
  );

  return (
    <div className="w-[420px]">
      <label htmlFor="therapeutic-area" className="text-[14px]/6">
        Therapeutic Area
      </label>
      {/* Only wrapped when the story asks for it, so every other story keeps the plain markup. */}
      {saving ? <SavingOverlay pending>{control}</SavingOverlay> : control}
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

/**
 * Mid-save, as a blur-saved form field looks: `SavingOverlay` freezes the control and puts a
 * spinner at its right edge, and the chevron and clear button get out of the way rather than
 * crowding it — they read `data-saving` off the group the overlay publishes.
 *
 * The spinner is deliberately delayed, so a save quick enough to beat it never disturbs the
 * chevron at all.
 */
export const Saving: Story = {
  args: { initial: OPTIONS[1], saving: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));

    // Still in the DOM, so the row keeps its width and the spinner lands where the chevron was.
    const chevron = canvas.getByLabelText('Show options');
    await expect(chevron).toBeInTheDocument();
    await expect(chevron).not.toBeVisible();
    await expect(canvas.getByLabelText('Clear selection')).not.toBeVisible();
  },
};

/** A reader who cannot edit: the pick still shows, nothing accepts input. */
export const Disabled: Story = {
  args: { initial: OPTIONS[1], disabled: true },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByRole('combobox')).toBeDisabled();
  },
};

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
    const trigger = within(canvasElement).getByRole('button', { name: 'Show options' });
    await userEvent.click(trigger);
    await expect(await screen.findByText('Searching…')).toBeInTheDocument();
    await expect(screen.queryByText('No matches')).not.toBeInTheDocument();

    await expect(trigger).toHaveAttribute('aria-busy', 'true');
    // The wait is reported in the popup and by aria-busy, never as a spinner at the field's right
    // edge — that spot means the field is being saved, and `SavingOverlay` owns it.
    await expect(trigger.querySelector('.animate-spin')).toBeNull();
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
