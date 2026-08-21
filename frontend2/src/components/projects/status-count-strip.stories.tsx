import { StatusCountStrip } from '@/components/projects/status-count-strip';
import { makeTotalCounts } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/StatusCountStrip',
  component: StatusCountStrip,
} satisfies Meta<typeof StatusCountStrip>;

export default meta;
type Story = StoryObj<typeof meta>;

export const AllStatuses: Story = { args: { counts: makeTotalCounts() } };

/** Zero-count statuses are dropped, so the strip shrinks to what has data. */
export const FewStatuses: Story = {
  args: { counts: makeTotalCounts({ experimentsByStatus: { OPEN: 12, SIGNED: 4 } }) },
};

export const Loading: Story = { args: { counts: undefined } };

export const NoExperiments: Story = {
  args: { counts: makeTotalCounts({ experimentsByStatus: {} }) },
};

/** The strip clips rather than squeezing its cells when the row is too narrow. */
export const Clipped: Story = {
  args: { counts: makeTotalCounts() },
  decorators: [
    (Story) => (
      <div className="w-[420px]">
        <Story />
      </div>
    ),
  ],
};
