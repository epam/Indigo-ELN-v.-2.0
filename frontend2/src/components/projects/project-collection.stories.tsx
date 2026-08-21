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

export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
};
