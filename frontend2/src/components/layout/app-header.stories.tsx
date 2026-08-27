import { expect, screen, userEvent, within } from 'storybook/test';

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

/**
 * The whole avatar + name block opens the user menu. Log Out is not clicked here — the
 * story router is a stub tree, and Storybook's `signOut` mock is a no-op either way.
 */
export const UserMenuOpen: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: 'Administrator' }));
    await expect(await screen.findByRole('menuitem', { name: 'Log Out' })).toBeInTheDocument();
  },
};

/** Enter in the header box opens the sheet with the term already in it. */
export const OpensGlobalSearch: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Search everything'), 'aspirin{Enter}');

    const panel = await screen.findByRole('dialog', { name: 'Global Search' });
    await expect(within(panel).getByLabelText('Quick search')).toHaveValue('aspirin');
  },
};
