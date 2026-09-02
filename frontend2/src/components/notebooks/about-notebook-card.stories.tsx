import { expect, within } from 'storybook/test';

import { AboutNotebookCard } from '@/components/notebooks/about-notebook-card';
import { makeNotebookDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/AboutNotebookCard',
  component: AboutNotebookCard,
  args: { notebook: makeNotebookDetails() },
} satisfies Meta<typeof AboutNotebookCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** A description is optional, and an absent one reads as a dash rather than a blank gap. */
export const NoDescription: Story = {
  args: { notebook: makeNotebookDetails({ description: undefined, attachments: [] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('-')).toBeInTheDocument();
  },
};

/** Without EDIT_NOTEBOOKS the pencil and every attachment control are gone. */
export const ReadOnly: Story = {
  args: { notebook: makeNotebookDetails({ currentPermissions: ['VIEW_NOTEBOOKS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('button', { name: 'Edit notebook' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Attach File' })).not.toBeInTheDocument();
    // The attachments themselves stay downloadable.
    await expect(canvas.getByRole('button', { name: /protocol\.docx/ })).toBeInTheDocument();
  },
};
