import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ExperimentRefsCombobox } from '@/components/experiments/template/experiment-refs-combobox';
import { EXPERIMENT_REFS } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ExperimentRef } from '@/lib/types/experiments.ts';

/** The experiment doing the referencing — `00000112-0006`, which shares a prefix with the rest. */
const SELF = EXPERIMENT_REFS[3];
const CHOSEN = EXPERIMENT_REFS[4];

function Harness({ initial, disabled }: { initial: ExperimentRef[]; disabled?: boolean }) {
  const [value, setValue] = useState(initial);
  return (
    <div className="w-[420px]">
      <ExperimentRefsCombobox
        id="refs"
        experimentId={SELF.id}
        value={value}
        onValueChange={setValue}
        disabled={disabled}
      />
    </div>
  );
}

const meta = {
  title: 'Experiments/Template/ExperimentRefsCombobox',
  component: Harness,
  args: { initial: [CHOSEN] },
} satisfies Meta<typeof Harness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Disabled: Story = { args: { disabled: true } };

/**
 * `00000112` matches three refs: the experiment being edited, one already held as a chip, and one
 * genuinely available. Only the last is offered — the other two would be a mistake and a no-op.
 */
export const HidesSelfAndChosen: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('combobox'), '00000112');

    // Portalled popup, so `screen` rather than the canvas.
    await waitFor(() => expect(screen.getByRole('option', { name: '00000112-0031' })).toBeInTheDocument());
    await expect(screen.queryByRole('option', { name: SELF.name })).not.toBeInTheDocument();
    await expect(screen.queryByRole('option', { name: CHOSEN.name })).not.toBeInTheDocument();
  },
};
