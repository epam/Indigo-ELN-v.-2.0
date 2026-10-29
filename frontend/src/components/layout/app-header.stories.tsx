import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { AppHeader } from '@/components/layout/app-header';
import { ApiError } from '@/lib/api';
import { notifyError } from '@/lib/toast';
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

/** The header button opens an empty report; submitting it closes the dialog and confirms. */
export const ReportsError: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Report Error' }));

    const dialog = await screen.findByRole('dialog', { name: 'Report Error' });
    await userEvent.type(within(dialog).getByLabelText(/Title/), 'Save button does nothing');
    await userEvent.type(within(dialog).getByLabelText(/Problem Description/), 'Clicked Save, nothing happened');
    await userEvent.click(within(dialog).getByRole('button', { name: 'Submit' }));

    await expect(await screen.findByText('Error report submitted successfully.')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByRole('dialog', { name: 'Report Error' })).not.toBeInTheDocument());
  },
};

/** An error toast's action opens the same dialog, seeded with the failure and the request behind it. */
export const ReportsErrorFromToast: Story = {
  play: async () => {
    notifyError(
      new ApiError(500, 'Internal Server Error', { path: '/api/eln/projects', method: 'POST', json: { name: 'x' } }),
    );
    // By text, not role: an unfocused toast's controls are outside the accessibility tree.
    await userEvent.click(await screen.findByText('Report Error'));

    const dialog = await screen.findByRole('dialog', { name: 'Report Error' });
    await expect(within(dialog).getByLabelText(/Title/)).toHaveValue('Backend error 500');
    await expect(within(dialog).getByLabelText(/Problem Description/)).toHaveValue(
      'Server error. Please try again later',
    );
    await expect(within(dialog).getByLabelText('Request URL')).toHaveValue('/api/eln/projects');
    await expect(within(dialog).getByLabelText('Request Body')).toHaveValue('{"name":"x"}');
    await expect(within(dialog).getByLabelText('Response Body')).toHaveValue('Internal Server Error');
  },
};
