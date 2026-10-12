import { expect, within } from 'storybook/test';

import { NotebookCollection } from '@/components/notebooks/list/notebook-collection';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/List/NotebookCollection',
  component: NotebookCollection,
  args: {
    projectId: '11111111-1111-1111-1111-111111111111',
    filters: { search: '', sort: 'LATEST', createdByMe: false },
    view: 'grid',
  },
  argTypes: { view: { control: 'inline-radio', options: ['grid', 'list'] } },
} satisfies Meta<typeof NotebookCollection>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Grid: Story = {};

export const List: Story = { args: { view: 'list' } };

export const Loading: Story = { parameters: { msw: { handlers: loadingHandlers } } };

export const Empty: Story = { parameters: { msw: { handlers: emptyHandlers } } };

/** apiFetch toasts the failure; this explains the missing list, in the same words. */
export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load notebooks/)).toBeInTheDocument();
  },
};
