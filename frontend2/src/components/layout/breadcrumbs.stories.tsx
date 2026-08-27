import { Breadcrumbs } from '@/components/layout/breadcrumbs';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Layout/Breadcrumbs',
  component: Breadcrumbs,
} satisfies Meta<typeof Breadcrumbs>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The projects page: a single item, so it is the current page. */
export const Single: Story = {
  args: { items: [{ label: 'All Projects' }] },
};

/** The project details page before the project has loaded. */
export const ProjectLoading: Story = {
  args: {
    items: [{ label: 'All Projects', link: { to: '/projects' } }, { label: 'Project:' }],
  },
};

export const ProjectLoaded: Story = {
  args: {
    items: [{ label: 'All Projects', link: { to: '/projects' } }, { label: 'Project: Test Project' }],
  },
};

/** Only the current page truncates; the trail ahead of it keeps its full width. */
export const LongLabel: Story = {
  args: {
    items: [
      { label: 'All Projects', link: { to: '/projects' } },
      { label: 'Project: Asymmetric Hydrogenation of Prochiral Ketones — Batch 47' },
    ],
  },
  render: (args) => (
    <div className="w-[380px]">
      <Breadcrumbs {...args} />
    </div>
  ),
};
