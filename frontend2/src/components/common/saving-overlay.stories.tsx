import { Redo2, Undo2 } from 'lucide-react';
import { expect, screen, waitFor, within } from 'storybook/test';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { Button } from '@/components/ui/button';
import { Combobox } from '@/components/ui/combobox';
import { Input } from '@/components/ui/input';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Common/SavingOverlay',
  component: SavingOverlay,
  args: {
    pending: false,
    children: <Input aria-label="Title" defaultValue="Acetic anhydride route" />,
  },
} satisfies Meta<typeof SavingOverlay>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Idle it is invisible — no wrapper chrome, no reserved space. */
export const Idle: Story = {};

/**
 * A field mid-save. The spinner is centred on the field's right edge — where a combobox's chevron
 * sits — and absolutely positioned, so the control does not move when it appears. The region is
 * inert rather than each control being disabled by hand.
 */
export const SavingField: Story = {
  args: { pending: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    // Nothing shows for the first 300ms — a save that beats that is silent by design.
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));

    const frozen = canvas.getByLabelText('Title').closest('[inert]');
    await expect(frozen).not.toBeNull();
    // The spinner and its status live outside that subtree: `inert` would strip them from the
    // accessibility tree along with everything else in it.
    await expect(frozen).not.toContainElement(screen.getByRole('status'));
  },
};

/** The page-level roll-up: the same primitive over a small control group, centred. */
export const SavingGroup: Story = {
  args: {
    pending: true,
    spinner: 'center',
    children: (
      <div className="flex items-center gap-2">
        <Button variant="outline" size="icon-lg" aria-label="Undo" disabled>
          <Undo2 />
        </Button>
        <Button variant="outline" size="icon-lg" aria-label="Redo" disabled>
          <Redo2 />
        </Button>
      </div>
    ),
  },
};

/**
 * A control whose own trailing edge would collide with the spinner stands aside: the combobox
 * hides its chevron and clear button off `data-saving` on the group this publishes. `invisible`,
 * not `hidden`, so the row keeps its width and the spinner lands where the chevron was.
 */
export const SavingACombobox: Story = {
  args: {
    pending: true,
    children: (
      <Combobox<string>
        id="area"
        aria-label="Therapeutic Area"
        value="Oncology"
        onValueChange={() => {}}
        items={['Oncology', 'Cardiology']}
      />
    ),
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));

    // Present for layout, but not competing with the spinner for the same corner.
    const chevron = canvas.getByLabelText('Show options');
    await expect(chevron).toBeInTheDocument();
    await expect(chevron).not.toBeVisible();
  },
};

/**
 * `top` instead, for a control tall enough that the centre is content rather than chrome: a
 * centred spinner would sit on the middle of the prose.
 */
export const SavingTallControl: Story = {
  args: {
    pending: true,
    spinner: 'top',
    children: (
      <div className="min-h-32 rounded-md border border-neutral-300 p-3 text-[14px]/6 text-neutral-1000">
        To a suspension of salicylic acid in acetic anhydride, add anhydrous sodium acetate with stirring. Heat the
        reaction mixture for approximately fifteen minutes.
      </div>
    ),
  },
};
