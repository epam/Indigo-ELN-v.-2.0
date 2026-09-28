import { DictionaryRow } from '@/components/dictionaries/dictionary-row';
import { DICTIONARY_LIST } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const [HANDLING, COMPOUND, SALT] = DICTIONARY_LIST;

const meta = {
  title: 'Dictionaries/DictionaryRow',
  component: DictionaryRow,
  args: { dictionary: COMPOUND, selected: false, onSelect: () => {} },
} satisfies Meta<typeof DictionaryRow>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The row whose sheet is open, which is how the reader keeps their place behind it. */
export const Selected: Story = { args: { selected: true } };

/** `description` is `@NotEmpty` on the DTO but empty in the seed data, so the em dash is not rare. */
export const NoDescription: Story = { args: { dictionary: HANDLING } };

/** Rows are meant to be read as a stack — this is what the page looks like. */
export const Stacked: Story = {
  render: () => (
    <div className="flex flex-col gap-3">
      <DictionaryRow dictionary={HANDLING} selected onSelect={() => {}} />
      <DictionaryRow dictionary={COMPOUND} selected={false} onSelect={() => {}} />
      <DictionaryRow dictionary={SALT} selected={false} onSelect={() => {}} />
    </div>
  ),
};
