import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { StatsBar } from '@/components/projects/list/stats-bar';
import { loadingHandlers, restrictedUserHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/List/StatsBar',
  component: StatsBar,
} satisfies Meta<typeof StatsBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Both children render their own skeletons off the same undefined counts. */
export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};

/** Add Project opens the create-project dialog. */
export const OpensAddProject: Story = {
  play: async ({ canvasElement }) => {
    const button = within(canvasElement).getByRole('button', { name: 'Add Project' });
    // Disabled until currentUser resolves, so it never flickers from enabled to disabled.
    await waitFor(() => expect(button).toBeEnabled());

    await userEvent.click(button);
    await waitFor(() => expect(screen.getByRole('heading', { name: 'Add Project' })).toBeInTheDocument());
  },
};

/** Without CREATE_PROJECTS the button is present but disabled, never hidden. */
export const CannotCreate: Story = {
  parameters: { msw: { handlers: restrictedUserHandlers } },
  play: async ({ canvasElement }) => {
    const button = within(canvasElement).getByRole('button', { name: 'Add Project' });
    await waitFor(() => expect(button).toBeDisabled());
  },
};
