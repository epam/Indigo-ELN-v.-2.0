import { expect, within } from 'storybook/test';

import { ExperimentCard } from '@/components/experiments/experiment-card';
import { makeAclEntry, makeExperiment } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/ExperimentCard',
  component: ExperimentCard,
  args: {
    item: makeExperiment({
      acl: [makeAclEntry('Administrator', { level: 'AUTHOR' }), makeAclEntry('Mark Liu')],
      aclCount: 6,
    }),
  },
} satisfies Meta<typeof ExperimentCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The name is generated and read-only: no endpoint can change it, so nothing offers to. */
export const NameIsReadOnly: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('00000001-0001')).toBeInTheDocument();
    await expect(canvas.queryByRole('textbox')).not.toBeInTheDocument();
  },
};

/** The star's two forms, which is the whole reason it is filled rather than only coloured. */
export const Starred: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Remove from starred' })).toHaveAttribute('aria-pressed', 'true');
  },
};

export const NotStarred: Story = {
  args: { item: makeExperiment({ marked: false }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Add to starred' })).toHaveAttribute('aria-pressed', 'false');
  },
};

/** The longest status label, which the badge has to grow for rather than truncate. */
export const LongStatus: Story = {
  args: { item: makeExperiment({ status: 'COMPLETED' }) },
};
