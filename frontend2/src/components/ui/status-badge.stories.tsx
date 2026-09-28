import { StatusBadge } from '@/components/ui/status-badge';
import { EXPERIMENT_STATUS_LABELS, EXPERIMENT_STATUSES } from '@/lib/types/experiments.ts';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/StatusBadge',
  component: StatusBadge,
  parameters: { layout: 'centered' },
  argTypes: {
    variant: { control: 'select', options: EXPERIMENT_STATUSES },
  },
} satisfies Meta<typeof StatusBadge>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  args: { variant: 'OPEN', children: 'Open' },
};

/** The variant keys are the ExperimentStatus values verbatim — all nine at once. */
export const AllStatuses: Story = {
  args: { children: 'Open' },
  render: () => (
    <div className="flex flex-col items-start gap-2">
      {EXPERIMENT_STATUSES.map((status) => (
        <StatusBadge key={status} variant={status}>
          {EXPERIMENT_STATUS_LABELS[status]}
        </StatusBadge>
      ))}
    </div>
  ),
};
