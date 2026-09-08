import { useState } from 'react';
import { expect, screen, userEvent, within } from 'storybook/test';

import { StatusFilterMenu } from '@/components/experiments/list/status-filter-menu';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ExperimentStatus } from '@/lib/types/experiments.ts';

/** Controlled by the URL in the app; by local state here, so the ticks actually move. */
function StatusFilterMenuHarness({ initial }: { initial: ExperimentStatus[] }) {
  const [value, setValue] = useState(initial);
  return (
    <div className="flex flex-col gap-2">
      <StatusFilterMenu value={value} onValueChange={setValue} />
      <p data-testid="selected" className="text-[14px]/6">
        {value.join(',')}
      </p>
    </div>
  );
}

const meta = {
  title: 'Experiments/List/StatusFilterMenu',
  component: StatusFilterMenuHarness,
  args: { initial: [] },
} satisfies Meta<typeof StatusFilterMenuHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Nothing ticked means no filter, so the trigger carries no count. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Status' })).toBeInTheDocument();
  },
};

export const WithSelection: Story = {
  args: { initial: ['OPEN', 'SIGNING'] },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Status (2)' })).toBeInTheDocument();
  },
};

/**
 * The popup stays open across several ticks — Base UI leaves `closeOnClick` false on a
 * checkbox item, which is what a multi-select filter wants.
 */
export const StaysOpenAcrossTicks: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Status' }));

    await userEvent.click(await screen.findByRole('menuitemcheckbox', { name: 'Signing' }));
    await userEvent.click(await screen.findByRole('menuitemcheckbox', { name: 'Open' }));

    // Rebuilt from EXPERIMENT_STATUSES, so the order is the display order, not the click order.
    await expect(canvas.getByTestId('selected')).toHaveTextContent('OPEN,SIGNING');
  },
};

/** Unticking removes just that one and leaves the rest alone. */
export const Untick: Story = {
  args: { initial: ['OPEN', 'SIGNING'] },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Status (2)' }));
    await userEvent.click(await screen.findByRole('menuitemcheckbox', { name: 'Open' }));

    await expect(canvas.getByTestId('selected')).toHaveTextContent('SIGNING');
  },
};
