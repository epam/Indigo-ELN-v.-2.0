import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';

import { SearchInput } from '@/components/ui/search-input';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** Controlled, so every story drives it from local state rather than from an arg. */
function Harness({ initial = '', placeholder }: { initial?: string; placeholder?: string }) {
  const [value, setValue] = useState(initial);

  return (
    <div className="w-[420px]">
      <SearchInput aria-label="Search materials" placeholder={placeholder} value={value} onChange={setValue} />
    </div>
  );
}

const meta = {
  title: 'UI/SearchInput',
  component: Harness,
} satisfies Meta<typeof Harness>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * `type="search"` makes it a `searchbox`, and the magnifier is decorative — the box has no
 * visible label, so an announced icon would be the only thing between it and its `aria-label`.
 */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const box = canvas.getByRole('searchbox', { name: 'Search materials' });
    await userEvent.type(box, 'pyridine');
    await expect(box).toHaveValue('pyridine');
    // The magnifier is `aria-hidden`, so the field's accessible name is the label alone.
    await expect(canvas.getByLabelText('Search materials')).toBe(box);
  },
};

export const WithValue: Story = { args: { initial: 'acetic anhydride' } };

/** The placeholder is overridable; "Search" is only the default. */
export const CustomPlaceholder: Story = { args: { placeholder: 'Filter batches' } };
