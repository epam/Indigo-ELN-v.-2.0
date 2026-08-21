import { fn } from 'storybook/test';

import { ActionBar } from '@/components/projects/action-bar';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/ActionBar',
  component: ActionBar,
  args: {
    search: '',
    sort: 'LATEST',
    createdByMe: false,
    view: 'grid',
    onSearchChange: fn(),
    onSortChange: fn(),
    onCreatedByMeChange: fn(),
    onViewChange: fn(),
  },
  argTypes: {
    sort: { control: 'inline-radio', options: ['EARLIEST', 'LATEST'] },
    view: { control: 'inline-radio', options: ['grid', 'list'] },
  },
} satisfies Meta<typeof ActionBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithSearch: Story = { args: { search: 'kinase' } };

export const FiltersActive: Story = {
  args: { search: 'kinase', sort: 'EARLIEST', createdByMe: true, view: 'list' },
};
