import { StoichiometryPanel } from '@/components/experiments/template/stoichiometry-panel';

import type { Meta, StoryObj } from '@storybook/react-vite';

const ALL_BLOCKS = {
  type: 'stoichiometryTable',
  reactionScheme: true,
  reactantsReagentsSolvents: true,
  intendedProducts: true,
} as const;

const meta = {
  title: 'Experiments/Template/StoichiometryPanel',
  component: StoichiometryPanel,
  args: {
    component: ALL_BLOCKS,
    reactions: [{ anchor: 'r1' }],
  },
} satisfies Meta<typeof StoichiometryPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Every block the template can ask for, and the single step every experiment has today. */
export const Default: Story = {};

/** A template that asks only for the scheme renders only the scheme. */
export const SchemeOnly: Story = {
  args: {
    component: { ...ALL_BLOCKS, reactantsReagentsSolvents: false, intendedProducts: false },
  },
};

/** The step strip with more than one reaction — what the design shows, and what the model allows. */
export const MultipleSteps: Story = {
  args: { reactions: [{ anchor: 'r1' }, { anchor: 'r2' }, { anchor: 'r3' }] },
};
