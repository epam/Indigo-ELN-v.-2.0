import { ProjectRow } from '@/components/projects/list/project-row';
import { makeProject } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/List/ProjectRow',
  component: ProjectRow,
  args: { item: makeProject() },
} satisfies Meta<typeof ProjectRow>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const LongName: Story = {
  args: {
    item: makeProject({
      name: 'Palladium-Catalysed Cross-Coupling Route Scouting for Intermediate B-17',
    }),
  },
};

export const NoExperiments: Story = {
  args: {
    item: makeProject({ notebookCount: 0, experimentCount: 0, experimentCountByStatus: {} }),
  },
};

/** Rows are meant to be read as a stack — this is what the list view looks like. */
export const Stacked: Story = {
  render: () => (
    <div className="flex flex-col gap-3">
      <ProjectRow item={makeProject()} />
      <ProjectRow item={makeProject({ id: 'b', name: 'Fragment Library Expansion' })} />
      <ProjectRow item={makeProject({ id: 'c', name: 'Route Scouting — Intermediate B' })} />
    </div>
  ),
};
