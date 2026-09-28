import { ProjectHeader } from '@/components/projects/details/project-header';
import { makeProjectDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/Details/ProjectHeader',
  component: ProjectHeader,
  args: {
    projectId: '11111111-1111-1111-1111-111111111111',
    project: makeProjectDetails(),
  },
} satisfies Meta<typeof ProjectHeader>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/**
 * Before the detail resolves: the trail keeps its bare label and the counts show skeletons,
 * so the header is the same height either way and the page below never jumps.
 */
export const Loading: Story = { args: { project: undefined } };

/** A project with nothing in it yet — the status strip falls back to a single zero cell. */
export const EmptyProject: Story = {
  args: {
    project: makeProjectDetails({ notebookCount: 0, experimentCount: 0, experimentCountByStatus: {} }),
  },
};

export const LongName: Story = {
  args: {
    project: makeProjectDetails({
      name: 'Kinase Inhibitor Screening — Series 4 Follow-up and Selectivity Profiling',
    }),
  },
};
