import { ProjectCard } from '@/components/projects/project-card';
import { makeAclEntry, makeProject } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/ProjectCard',
  component: ProjectCard,
  args: { item: makeProject() },
  decorators: [
    (Story) => (
      <div className="w-[320px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ProjectCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The title truncates rather than wrapping, keeping every card the same height. */
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

/** acl is capped by the backend, so the stack ends in a +N badge. */
export const ManyCollaborators: Story = {
  args: {
    item: makeProject({
      acl: [makeAclEntry('Administrator'), makeAclEntry('Mark Liu'), makeAclEntry('Sofia Rossi')],
      aclCount: 12,
    }),
  },
};
