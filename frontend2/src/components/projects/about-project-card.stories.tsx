import { expect, screen, userEvent, within } from 'storybook/test';

import { AboutProjectCard } from '@/components/projects/about-project-card';
import { makeProjectDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Projects/AboutProjectCard',
  component: AboutProjectCard,
  args: { project: makeProjectDetails() },
} satisfies Meta<typeof AboutProjectCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Empty optional fields read as `-` rather than collapsing the section away. */
export const Sparse: Story = {
  args: {
    project: makeProjectDetails({
      keywords: [],
      literature: undefined,
      description: undefined,
      attachments: [],
    }),
  },
};

/** Without EDIT_PROJECTS the pencil and the whole attachment-editing surface are gone. */
export const ReadOnly: Story = {
  args: { project: makeProjectDetails({ currentPermissions: ['VIEW_PROJECTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('button', { name: 'Edit project' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Attach File' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: /^Delete / })).not.toBeInTheDocument();
    // The attachments themselves are still listed and still downloadable.
    await expect(canvas.getByRole('button', { name: /protocol\.docx/ })).toBeInTheDocument();
  },
};

/** The pencil opens the shared project dialog in edit mode, seeded with what is on screen. */
export const Editing: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Edit project' }));
    // The dialog is portalled, so it lives outside canvasElement.
    await expect(await screen.findByText('Edit Project')).toBeInTheDocument();
    await expect(screen.getByLabelText(/Project Name/)).toHaveValue('Kinase Inhibitor Screening');
  },
};
