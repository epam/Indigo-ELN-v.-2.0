import { StatsBar } from '@/components/projects/stats-bar';
import { loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/StatsBar',
  component: StatsBar,
} satisfies Meta<typeof StatsBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Both children render their own skeletons off the same undefined counts. */
export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};
