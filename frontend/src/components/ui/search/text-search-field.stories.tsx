import { useState } from 'react';
import { expect, screen, userEvent, within } from 'storybook/test';

import { TextSearchField } from '@/components/ui/search/text-search-field';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { TextSearch } from '@/lib/types/search.ts';

/** Controlled by the story, so what the field emits is what the next render shows. */
function Harness({ disabled }: { disabled?: boolean }) {
  const [value, setValue] = useState<TextSearch | null>(null);

  return (
    <div className="flex w-80 flex-col gap-2">
      <TextSearchField
        id="compound-id"
        label="Compound ID"
        value={value}
        onValueChange={setValue}
        disabled={disabled}
      />
      <output className="text-[12px]/5 text-neutral-700">{value == null ? 'null' : JSON.stringify(value)}</output>
    </div>
  );
}

const meta = {
  title: 'Search/TextSearchField',
  component: Harness,
  args: {},
} satisfies Meta<typeof Harness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** A blank box is not a filter, so nothing is emitted until something is typed. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('null')).toBeInTheDocument();

    await userEvent.type(canvas.getByRole('textbox'), 'ASA');

    await expect(canvas.getByText('{"type":"exact","value":"ASA"}')).toBeInTheDocument();
  },
};

/** The operator picker is a Menu, so its items are portalled out of the canvas. */
export const ChoosingAnOperator: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('textbox'), 'ASA');

    await userEvent.click(canvas.getByRole('button', { name: 'Compound ID operator' }));
    await userEvent.click(await screen.findByRole('menuitem', { name: 'contains' }));

    await expect(canvas.getByText('{"type":"contains","value":"ASA"}')).toBeInTheDocument();
  },
};

/** `between` is the one member with two boxes, and either bound on its own is a range. */
export const Between: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Compound ID operator' }));
    await userEvent.click(await screen.findByRole('menuitem', { name: 'between' }));

    await userEvent.type(canvas.getByRole('textbox', { name: 'Compound ID from' }), '1');

    await expect(canvas.getByText('{"type":"between","from":"1","to":""}')).toBeInTheDocument();
  },
};

/** What a PubChem catalog forces: readable, but nothing can be changed. */
export const Disabled: Story = {
  args: { disabled: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('textbox')).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Compound ID operator' })).toBeDisabled();
  },
};
