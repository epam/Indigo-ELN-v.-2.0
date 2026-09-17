import { fn } from 'storybook/test';

import { ActionBar } from '@/components/common/action-bar';
import { Button } from '@/components/ui/button';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Common/ActionBar',
  component: ActionBar,
  args: {
    entityLabel: 'projects',
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

/** The `children` slot, where experiments will hang their status multiselect. */
export const WithExtraFilter: Story = {
  args: {
    entityLabel: 'experiments',
    children: (
      <Button variant="secondary" size="lg" className="rounded-md">
        Status
      </Button>
    ),
  },
};
