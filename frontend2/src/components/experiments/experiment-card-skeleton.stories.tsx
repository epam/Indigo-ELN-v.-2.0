import { ExperimentCardSkeleton } from '@/components/experiments/experiment-card-skeleton';
import { ExperimentRowSkeleton } from '@/components/experiments/experiment-row-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/ExperimentSkeletons',
  component: ExperimentCardSkeleton,
} satisfies Meta<typeof ExperimentCardSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Card: Story = {};

export const Row: Story = { render: () => <ExperimentRowSkeleton /> };
