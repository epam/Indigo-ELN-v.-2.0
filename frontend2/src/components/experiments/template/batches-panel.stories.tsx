import { BatchesPanel } from '@/components/experiments/template/batches-panel';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/Template/BatchesPanel',
  component: BatchesPanel,
} satisfies Meta<typeof BatchesPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The stub as the template registry renders it, until the real body lands. */
export const Default: Story = {};
