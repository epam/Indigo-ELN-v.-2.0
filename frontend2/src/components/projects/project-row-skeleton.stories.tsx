import { ProjectRowSkeleton } from '@/components/projects/project-row-skeleton';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/ProjectRowSkeleton',
  component: ProjectRowSkeleton,
} satisfies Meta<typeof ProjectRowSkeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Must match ProjectRow's height, or the first page shifts the layout on load. */
export const Default: Story = {};
