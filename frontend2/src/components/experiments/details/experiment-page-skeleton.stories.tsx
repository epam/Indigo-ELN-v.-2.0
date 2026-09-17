import { ExperimentPageSkeleton } from '@/components/experiments/details/experiment-page-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/Details/ExperimentPageSkeleton',
  component: ExperimentPageSkeleton,
} satisfies Meta<typeof ExperimentPageSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
