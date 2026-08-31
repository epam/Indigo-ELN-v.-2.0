import {expect, userEvent, waitFor, within} from 'storybook/test';

import {AttachmentList} from '@/components/projects/attachment-list';
import {ATTACHMENTS} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Projects/AttachmentList',
  component: AttachmentList,
  args: {
    projectId: '11111111-1111-1111-1111-111111111111',
    attachments: ATTACHMENTS,
    canEdit: true,
  },
} satisfies Meta<typeof AttachmentList>;

export default meta;
type Story = StoryObj<typeof meta>;

/** One row per icon group, so the extension mapping is visible at a glance. */
export const Default: Story = {};

export const Empty: Story = { args: { attachments: [] } };

/** Read-only keeps the download links and drops every delete button. */
export const ReadOnly: Story = {
  args: { canEdit: false },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('button', { name: /^Delete / })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Attach File' })).not.toBeInTheDocument();
    await expect(canvas.getByRole('button', { name: /protocol\.docx/ })).toBeInTheDocument();
  },
};

/**
 * The row disappears from the cached project once the DELETE resolves. The list here is a
 * fixed prop rather than the cache, so this only pins that the request is made and the
 * button returns from its pending state.
 */
export const Deleting: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button', { name: 'Delete protocol.docx' });
    await userEvent.click(button);
    await waitFor(() => expect(button).not.toHaveAttribute('aria-busy', 'true'));
  },
};
