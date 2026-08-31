import { VersionHistoryPanel } from '@/components/experiments/template/version-history-panel';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/Template/VersionHistoryPanel',
  component: VersionHistoryPanel,
} satisfies Meta<typeof VersionHistoryPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The stub as the template registry renders it, until the real body lands. */
export const Default: Story = {};
