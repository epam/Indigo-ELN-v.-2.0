import { NotebookInfoSkeleton } from '@/components/notebooks/details/notebook-info-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/Details/NotebookInfoSkeleton',
  component: NotebookInfoSkeleton,
} satisfies Meta<typeof NotebookInfoSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
