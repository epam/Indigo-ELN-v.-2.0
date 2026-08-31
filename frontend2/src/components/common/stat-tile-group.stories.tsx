import {StatTileGroup} from '@/components/common/stat-tile-group';
import {makeTotalCounts} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Common/StatTileGroup',
  component: StatTileGroup,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof StatTileGroup>;

export default meta;
type Story = StoryObj<typeof meta>;

const counts = makeTotalCounts();

export const Default: Story = {
  args: {
    tiles: [
      { key: 'projects', count: counts.projects },
      { key: 'notebooks', count: counts.notebooks },
      { key: 'experiments', count: counts.experiments },
    ],
  },
};

/** `count: undefined` is the loading state — tiles keep their size. */
export const Loading: Story = {
  args: {
    tiles: [
      { key: 'projects', count: undefined },
      { key: 'notebooks', count: undefined },
      { key: 'experiments', count: undefined },
    ],
  },
};

export const Zeroes: Story = {
  args: {
    tiles: [
      { key: 'projects', count: 0 },
      { key: 'notebooks', count: 0 },
      { key: 'experiments', count: 0 },
    ],
  },
};

/** A project shows only what it contains — the group renders whichever tiles it is given. */
export const ProjectPair: Story = {
  args: {
    tiles: [
      { key: 'notebooks', count: 8 },
      { key: 'experiments', count: 13 },
    ],
  },
};
