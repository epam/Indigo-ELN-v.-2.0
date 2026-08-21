import { ProjectCardSkeleton } from '@/components/projects/project-card-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/ProjectCardSkeleton',
  component: ProjectCardSkeleton,
  decorators: [
    (Story) => (
      <div className="w-[320px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ProjectCardSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Must match ProjectCard's height, or the first page shifts the layout on load. */
export const Default: Story = {};
