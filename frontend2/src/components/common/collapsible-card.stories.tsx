import { Download, Upload } from 'lucide-react';

import { CollapsibleCard } from '@/components/common/collapsible-card';
import { Button } from '@/components/ui/button';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Common/CollapsibleCard',
  component: CollapsibleCard,
  args: {
    title: 'Experiment Details',
    children: <p className="text-[14px]/6 text-neutral-700">The card body goes here.</p>,
  },
} satisfies Meta<typeof CollapsibleCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** A card that starts folded — the chevron points down until it is opened. */
export const Collapsed: Story = {
  args: { defaultOpen: false },
};

/** The actions sit beside the trigger, so clicking one does not fold the card. */
export const WithActions: Story = {
  args: {
    title: 'Stoichiometric Calculation',
    actions: (
      <>
        <Button variant="ghost" size="icon" aria-label="Export">
          <Download />
        </Button>
        <Button variant="ghost" size="icon" aria-label="Import">
          <Upload />
        </Button>
      </>
    ),
  },
};

/** A title long enough to need the truncation the header row relies on. */
export const LongTitle: Story = {
  args: { title: 'Reactants, Reagents, Solvents and Intended Products for the Second Reaction Step' },
};
