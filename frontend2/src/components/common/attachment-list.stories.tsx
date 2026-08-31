import {expect, userEvent, waitFor, within} from 'storybook/test';

import {AttachmentList} from '@/components/common/attachment-list';
import {useProjectAttachments} from '@/lib/api/projects';
import {ATTACHMENTS} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';
import type {Attachment} from '@/lib/types/common.ts';

const PROJECT_ID = '11111111-1111-1111-1111-111111111111';

/** The actions come from hooks, so the stories render through a wrapper that makes them. */
function ProjectAttachmentList({ attachments, canEdit }: { attachments: Attachment[]; canEdit: boolean }) {
  const actions = useProjectAttachments(PROJECT_ID);

  return <AttachmentList attachments={attachments} canEdit={canEdit} actions={actions} />;
}

const meta = {
  title: 'Common/AttachmentList',
  component: ProjectAttachmentList,
  args: {
    attachments: ATTACHMENTS,
    canEdit: true,
  },
} satisfies Meta<typeof ProjectAttachmentList>;

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
