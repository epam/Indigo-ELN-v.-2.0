import { NotebookHeader } from '@/components/notebooks/notebook-header';
import { makeNotebookDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/NotebookHeader',
  component: NotebookHeader,
  args: {
    notebookId: '77777777-7777-7777-7777-777777777777',
    notebook: makeNotebookDetails(),
  },
} satisfies Meta<typeof NotebookHeader>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/**
 * Before the detail resolves: the trail keeps its bare labels and the count shows a skeleton,
 * so the header is the same height either way and the page below never jumps.
 */
export const Loading: Story = { args: { notebook: undefined } };

/** A notebook with nothing in it yet — the status strip falls back to a single zero cell. */
export const EmptyNotebook: Story = {
  args: {
    notebook: makeNotebookDetails({ experimentCount: 0, experimentCountByStatus: {} }),
  },
};

/** The trail is the first thing to give way, so a long project name has to clip rather than wrap. */
export const LongProjectName: Story = {
  args: {
    notebook: makeNotebookDetails({
      projectName: 'Kinase Inhibitor Screening — Series 4 Follow-up and Selectivity Profiling',
    }),
  },
};
