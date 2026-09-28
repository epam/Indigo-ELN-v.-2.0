import { Skeleton } from '@/components/ui/skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Skeleton',
  component: Skeleton,
} satisfies Meta<typeof Skeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Line: Story = { args: { className: 'h-4 w-64' } };

export const Block: Story = { args: { className: 'h-24 w-64' } };

/** The shapes the skeleton components compose: icon, text line, badge. */
export const Shapes: Story = {
  render: () => (
    <div className="flex items-center gap-2">
      <Skeleton className="size-4 shrink-0" />
      <Skeleton className="h-4 w-48" />
      <Skeleton className="h-[26px] w-[59px] shrink-0 rounded-md" />
    </div>
  ),
};
