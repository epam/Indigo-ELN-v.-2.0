import { expect, within } from 'storybook/test';

import { ProjectCollection } from '@/components/projects/project-collection';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/ProjectCollection',
  component: ProjectCollection,
  args: {
    filters: { search: '', sort: 'LATEST', createdByMe: false },
    view: 'grid',
  },
  argTypes: { view: { control: 'inline-radio', options: ['grid', 'list'] } },
} satisfies Meta<typeof ProjectCollection>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Grid: Story = {};

export const List: Story = { args: { view: 'list' } };

export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};

export const Empty: Story = {
  parameters: { msw: { handlers: emptyHandlers } },
};

/** apiFetch toasts the failure; this explains the missing list, in the same words. */
export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load projects/)).toBeInTheDocument();
    // The wording comes from describeError, not from ApiError's constructor string.
    await expect(canvas.getByText(/Server error\. Please try again later/)).toBeInTheDocument();
    await expect(canvas.queryByText(/Request failed with status/)).not.toBeInTheDocument();
  },
};
