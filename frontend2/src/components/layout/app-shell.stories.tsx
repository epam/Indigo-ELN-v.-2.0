import { AppShell } from '@/components/layout/app-shell';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Layout/AppShell',
  component: AppShell,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof AppShell>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Header, sidebar, and content well together — the frame every page renders into. */
export const Default: Story = {
  args: {
    children: <div className="rounded-6 bg-card p-4 shadow-card">Page content</div>,
  },
};
