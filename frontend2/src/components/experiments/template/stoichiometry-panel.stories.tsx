import { StoichiometryPanel } from '@/components/experiments/template/stoichiometry-panel';
import { makeExperimentDetails, makeReaction } from '@/mocks/fixtures';

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
    experiment: makeExperimentDetails(),
  },
} satisfies Meta<typeof StoichiometryPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Every block the template can ask for, over the single step every experiment has today. */
export const Default: Story = {};

/** A template that asks only for the scheme renders only the scheme. */
export const SchemeOnly: Story = {
  args: {
    component: { ...ALL_BLOCKS, reactantsReagentsSolvents: false, intendedProducts: false },
  },
};

/**
 * More than one reaction — the model allows it, even though nothing can create one yet. The step
 * strip is gated off (`SHOW_STEP_SELECTOR`), so this must render the *first* step and no strip;
 * it is here to catch a panel that starts depending on there being exactly one.
 */
export const MultipleSteps: Story = {
  args: {
    experiment: makeExperimentDetails({
      model: {
        significantFigures: 5,
        reactions: [
          makeReaction(),
          makeReaction({ anchor: 'b0000000-0000-4000-8000-000000000002' }),
          makeReaction({ anchor: 'b0000000-0000-4000-8000-000000000003' }),
        ],
      },
    }),
  },
};
