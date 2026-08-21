import { Badge } from '@/components/ui/badge';
import { EXPERIMENT_STATUS_DISPLAY, EXPERIMENT_STATUSES } from '@/lib/types/experiments.ts';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Badge',
  component: Badge,
  parameters: { layout: 'centered' },
  argTypes: {
    variant: { control: 'select', options: EXPERIMENT_STATUSES },
  },
} satisfies Meta<typeof Badge>;

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
        <Badge key={status} variant={status}>
          {EXPERIMENT_STATUS_DISPLAY[status]}
        </Badge>
      ))}
    </div>
  ),
};
