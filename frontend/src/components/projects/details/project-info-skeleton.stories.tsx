import { ProjectInfoSkeleton } from '@/components/projects/details/project-info-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/Details/ProjectInfoSkeleton',
  component: ProjectInfoSkeleton,
} satisfies Meta<typeof ProjectInfoSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

/** What the Project Info tab shows before the detail resolves. */
export const Default: Story = {};
