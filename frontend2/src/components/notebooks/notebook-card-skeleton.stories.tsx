import { NotebookCardSkeleton } from '@/components/notebooks/notebook-card-skeleton';
import { NotebookRowSkeleton } from '@/components/notebooks/notebook-row-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/NotebookSkeletons',
  component: NotebookCardSkeleton,
} satisfies Meta<typeof NotebookCardSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Card: Story = {
  decorators: [
    (Story) => (
      <div className="w-[360px]">
        <Story />
      </div>
    ),
  ],
};

export const Row: Story = { render: () => <NotebookRowSkeleton /> };
