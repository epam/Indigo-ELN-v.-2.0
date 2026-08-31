import { ExperimentPageSkeleton } from '@/components/experiments/experiment-page-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/ExperimentPageSkeleton',
  component: ExperimentPageSkeleton,
} satisfies Meta<typeof ExperimentPageSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
