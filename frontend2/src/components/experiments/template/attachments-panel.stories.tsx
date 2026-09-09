import { expect, userEvent, waitFor, within } from 'storybook/test';

import { AttachmentsPanel } from '@/components/experiments/template/attachments-panel';
import { useExperiment } from '@/lib/api/experiments';
import { makeExperimentDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();

const meta = {
  title: 'Experiments/Template/AttachmentsPanel',
  component: AttachmentsPanel,
  args: { experiment: EXPERIMENT },
} satisfies Meta<typeof AttachmentsPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** No heading of its own — the card around it is already titled *Attachments*. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('protocol.docx')).toBeInTheDocument();
    await expect(canvas.queryByRole('heading', { name: 'Attachments' })).not.toBeInTheDocument();
  },
};

/** An experiment with nothing attached yet — only the Attach File button. */
export const Empty: Story = {
  args: { experiment: makeExperimentDetails({ attachments: [] }) },
};

/** A reader who cannot edit gets no delete buttons and no upload control. */
export const ReadOnly: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('protocol.docx')).toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Attach File' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: /^Delete/ })).not.toBeInTheDocument();
  },
};

/** The other half of the gate: EDIT_EXPERIMENTS holds, but a completed experiment is not writable. */
export const Completed: Story = {
  args: { experiment: makeExperimentDetails({ status: 'COMPLETED' }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('protocol.docx')).toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: 'Attach File' })).not.toBeInTheDocument();
    await expect(canvas.queryByRole('button', { name: /^Delete/ })).not.toBeInTheDocument();
  },
};

/**
 * Deleting drops the row — but only through the query cache, which the panel reads via its
 * `experiment` prop. A story passing a fixed fixture could never show that, so this one reads
 * the experiment back out of the cache, which is also what makes it a real test of the round
 * trip. The same reasoning as `TeamCardFromCache` in `team-card.stories.tsx`.
 */
function AttachmentsFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <AttachmentsPanel experiment={data} /> : null;
}

export const DeleteAttachment: Story = {
  render: () => <AttachmentsFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: 'Delete protocol.docx' }));
    await waitFor(() => expect(canvas.queryByText('protocol.docx')).not.toBeInTheDocument());
    // The rest of the list is untouched.
    await expect(canvas.getByText('yields.xlsx')).toBeInTheDocument();
  },
};
