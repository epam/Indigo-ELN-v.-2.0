import {NotebookCard} from '@/components/notebooks/notebook-card';
import {makeNotebook, NOTEBOOKS} from '@/mocks/fixtures';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Notebooks/NotebookCard',
  component: NotebookCard,
  parameters: { layout: 'centered' },
  decorators: [
    (Story) => (
      <div className="w-[360px]">
        <Story />
      </div>
    ),
  ],
  args: { item: makeNotebook() },
} satisfies Meta<typeof NotebookCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** A brand-new notebook: no experiments, so the open count and the total are both zero. */
export const Empty: Story = { args: { item: NOTEBOOKS[2] } };

export const LongName: Story = {
  args: { item: makeNotebook({ name: '00000001 — Route scouting for the intermediate B series' }) },
};

/** The avatar stack keeps its own width; the overflow badge counts everyone the ACL left out. */
export const ManyMembers: Story = { args: { item: makeNotebook({ aclCount: 120 }) } };
