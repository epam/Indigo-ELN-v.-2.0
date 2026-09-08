import { expect, userEvent, waitFor, within } from 'storybook/test';

import { VersionHistoryPanel } from '@/components/experiments/template/version-history-panel';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Experiments/Template/VersionHistoryPanel',
  component: VersionHistoryPanel,
  args: { experiment: makeExperimentDetails() },
} satisfies Meta<typeof VersionHistoryPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * The whole log. `REVISIONS` is in backend order — oldest first — so the first data row being
 * the *last* fixture entry is what pins the reversal in `useExperimentRevisions`.
 */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByText('Version 1');

    const rows = canvas.getAllByRole('row');
    // rows[0] is the header.
    await expect(within(rows[1]).getByText('Version 1')).toBeInTheDocument();
    await expect(within(rows[rows.length - 1]).getByText('Experiment created')).toBeInTheDocument();

    // Revision 1 has nothing to diff against — `@Min(2)` on the endpoint.
    await expect(within(rows[rows.length - 1]).queryByRole('button')).not.toBeInTheDocument();
  },
};

/** A grouped edit session shows its span, and folds its revisions away until asked. */
export const ExpandGroup: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByText('Edited experiment');

    await expect(canvas.getByText('2–4')).toBeInTheDocument();
    await expect(canvas.queryByText('Set input amount')).not.toBeInTheDocument();

    await userEvent.click(canvas.getByRole('button', { name: /^Show the revisions in Edited experiment/ }));
    await expect(await canvas.findByText('Set input amount')).toBeInTheDocument();
    await expect(canvas.getByText('Set reaction scheme')).toBeInTheDocument();
  },
};

/** Show and hide one revision's diff, which is the only action the panel offers. */
export const ShowDiff: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const show = await canvas.findByRole('button', { name: 'Show the changes in revision 6' });

    await userEvent.click(show);
    await waitFor(() => expect(canvas.getByText('theoWeight:')).toBeInTheDocument());

    await userEvent.click(canvas.getByRole('button', { name: 'Hide the changes in revision 6' }));
    await waitFor(() => expect(canvas.queryByText('theoWeight:')).not.toBeInTheDocument());
  },
};

/**
 * The oldest revision in a group carries the group's own number — `revisionGroupToSummary` builds
 * the parent from `list.getFirst()` — and it is the child shown *last*, once the log is reversed.
 * So its diff is the one that used to render twice: under the row that was asked, and under the
 * group header, which has no diff button at all.
 */
export const ShowDiffOfOldestInGroup: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: /^Show the revisions in Edited experiment/ }));
    await userEvent.click(await canvas.findByRole('button', { name: 'Show the changes in revision 2' }));

    await waitFor(() => expect(canvasElement.querySelectorAll('.patch-grid')).toHaveLength(1));

    // And it sits under the row it was asked for — the last of the group, not the header above it.
    const rows = canvas.getAllByRole('row');
    const asked = rows.findIndex((row) => within(row).queryByText('Add empty input') !== null);
    const diff = rows.findIndex((row) => row.querySelector('.patch-grid') !== null);
    await expect(diff).toBe(asked + 1);
  },
};

/** Skeleton rows while the log loads. */
export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};

/** `apiFetch` has toasted already; the panel says which table is empty and why. */
export const LoadFailed: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load the revision log/)).toBeInTheDocument();
  },
};
