import { NotebookInfoSkeleton } from '@/components/notebooks/notebook-info-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/NotebookInfoSkeleton',
  component: NotebookInfoSkeleton,
} satisfies Meta<typeof NotebookInfoSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
