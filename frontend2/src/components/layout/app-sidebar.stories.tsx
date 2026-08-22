import { expect, userEvent, within } from 'storybook/test';

import { AppSidebar } from '@/components/layout/app-sidebar';
import { loadingHandlers, restrictedUserHandlers } from '@/mocks/handlers';

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

/** The default fixture user holds every gated permission, so the whole menu shows. */
export const Expanded: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByRole('link', { name: 'Dictionaries' })).toBeInTheDocument();
    await expect(canvas.getByRole('link', { name: 'Signatures' })).toBeInTheDocument();
  },
};

/** Without MANAGE_DICTIONARIES or SIGN_EXPERIMENTS only the unprotected item survives. */
export const Restricted: Story = {
  parameters: { msw: { handlers: restrictedUserHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // The starred list resolving means the parallel currentUser request has landed too,
    // so an absent nav item is a real absence and not a not-yet.
    await canvas.findByRole('link', { name: /00000001-0012/ });
    await expect(canvas.getByRole('link', { name: 'All Projects' })).toBeInTheDocument();
    await expect(canvas.queryByRole('link', { name: 'Dictionaries' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('link', { name: 'Signatures' })).not.toBeInTheDocument();
  },
};

/** currentUser never resolves — the gated items stay hidden rather than flashing in. */
export const LoadingUser: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('link', { name: 'All Projects' })).toBeInTheDocument();
    await expect(canvas.queryByRole('link', { name: 'Signatures' })).not.toBeInTheDocument();
  },
};

/** Collapsing is internal state, so the story drives it the way a user would. */
export const Collapsed: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Collapse sidebar' }));
    await expect(canvas.getByRole('button', { name: 'Expand sidebar' })).toBeInTheDocument();
  },
};
