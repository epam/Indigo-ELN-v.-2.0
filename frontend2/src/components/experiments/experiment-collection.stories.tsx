import {expect, within} from 'storybook/test';

import {ExperimentCollection} from '@/components/experiments/experiment-collection';
import {emptyHandlers, errorHandlers, loadingHandlers} from '@/mocks/handlers';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Experiments/ExperimentCollection',
  component: ExperimentCollection,
  args: {
    notebookId: '77777777-7777-7777-7777-777777777777',
    filters: { search: '', sort: 'LATEST', createdByMe: false, statuses: [] },
    view: 'grid',
  },
  argTypes: { view: { control: 'inline-radio', options: ['grid', 'list'] } },
} satisfies Meta<typeof ExperimentCollection>;

export default meta;
type Story = StoryObj<typeof meta>;

/** One card per status, so every Badge variant is on screen at once. */
export const Grid: Story = {};

export const List: Story = { args: { view: 'list' } };

/** The handler filters on `status`, so this is also a check that the param reaches it. */
export const FilteredByStatus: Story = {
  args: { filters: { search: '', sort: 'LATEST', createdByMe: false, statuses: ['OPEN', 'SIGNING'] } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText('00000001-0001')).toBeInTheDocument();
    await expect(canvas.queryByText('00000001-0003')).not.toBeInTheDocument();
  },
};

export const Loading: Story = { parameters: { msw: { handlers: loadingHandlers } } };

export const Empty: Story = { parameters: { msw: { handlers: emptyHandlers } } };

/** apiFetch toasts the failure; this explains the missing list, in the same words. */
export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load experiments/)).toBeInTheDocument();
  },
};
