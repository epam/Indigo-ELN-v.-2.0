import { DictionaryRowSkeleton } from '@/components/dictionaries/dictionary-row-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Dictionaries/DictionaryRowSkeleton',
  component: DictionaryRowSkeleton,
} satisfies Meta<typeof DictionaryRowSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** What the first load actually shows: a run of them, the same height as the rows that replace it. */
export const Stacked: Story = {
  render: () => (
    <div className="flex flex-col gap-3">
      {Array.from({ length: 4 }, (_, index) => (
        <DictionaryRowSkeleton key={index} />
      ))}
    </div>
  ),
};
