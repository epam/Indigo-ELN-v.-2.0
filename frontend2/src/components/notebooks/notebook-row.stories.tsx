import {NotebookRow} from '@/components/notebooks/notebook-row';
import {makeNotebook} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/NotebookRow',
  component: NotebookRow,
  args: { item: makeNotebook() },
} satisfies Meta<typeof NotebookRow>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** `auto-fit` drops columns as the row narrows rather than squeezing all four. */
export const Narrow: Story = {
  decorators: [
    (Story) => (
      <div className="w-[420px]">
        <Story />
      </div>
    ),
  ],
};
