import { StatTileGroup } from '@/components/projects/stat-tile-group';
import { makeTotalCounts } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/StatTileGroup',
  component: StatTileGroup,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof StatTileGroup>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = { args: { counts: makeTotalCounts() } };

/** `counts: undefined` is the loading state — tiles keep their size. */
export const Loading: Story = { args: { counts: undefined } };

export const Zeroes: Story = {
  args: { counts: makeTotalCounts({ projects: 0, notebooks: 0, experiments: 0 }) },
};
