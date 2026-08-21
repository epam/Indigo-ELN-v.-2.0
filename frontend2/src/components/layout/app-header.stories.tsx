import { AppHeader } from '@/components/layout/app-header';
import { loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Layout/AppHeader',
  component: AppHeader,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof AppHeader>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The avatar and name are omitted until currentUser resolves. */
export const LoadingUser: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};
