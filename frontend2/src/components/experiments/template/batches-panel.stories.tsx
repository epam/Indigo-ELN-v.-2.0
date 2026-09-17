import { BatchesPanel } from '@/components/experiments/template/batches-panel';
import { makeExperimentDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/Template/BatchesPanel',
  component: BatchesPanel,
  args: { experiment: makeExperimentDetails() },
} satisfies Meta<typeof BatchesPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The panel as the template registry renders it: its own folding card around the batch table. */
export const Default: Story = {};
