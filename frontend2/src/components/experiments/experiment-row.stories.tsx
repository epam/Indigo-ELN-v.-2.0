import {ExperimentRow} from '@/components/experiments/experiment-row';
import {makeAclEntry, makeExperiment} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Experiments/ExperimentRow',
  component: ExperimentRow,
  args: {
    item: makeExperiment({
      acl: [makeAclEntry('Administrator', { level: 'AUTHOR' }), makeAclEntry('Mark Liu')],
      aclCount: 6,
    }),
  },
} satisfies Meta<typeof ExperimentRow>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const NotStarred: Story = { args: { item: makeExperiment({ marked: false }) } };

export const Rejected: Story = { args: { item: makeExperiment({ status: 'REJECTED' }) } };
