import { expect, userEvent, within } from 'storybook/test';

import { AppSidebar } from '@/components/layout/app-sidebar';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Layout/AppSidebar',
  component: AppSidebar,
  decorators: [
    (Story) => (
      <div className="bg-neutral-200 p-4">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof AppSidebar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Expanded: Story = {};

/** Collapsing is internal state, so the story drives it the way a user would. */
export const Collapsed: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Collapse sidebar' }));
    await expect(canvas.getByRole('button', { name: 'Expand sidebar' })).toBeInTheDocument();
  },
};
