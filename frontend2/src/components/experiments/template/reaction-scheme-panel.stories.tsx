import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ReactionSchemePanel } from '@/components/experiments/template/reaction-scheme-panel';
import { makeExperimentDetails, makeReaction } from '@/mocks/fixtures';
import { failingMutateHandlers, slowMutateHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

/**
 * Ketcher is mocked in Storybook (`.storybook/mocks/`), so the sketcher is a placeholder that
 * always draws the same canned benzene and Save always hands back `mock-molfile`. What these
 * stories exercise is the round trip around it, not the chemistry.
 */
const EXPERIMENT = makeExperimentDetails();

const meta = {
  title: 'Experiments/Template/ReactionSchemePanel',
  component: ReactionSchemePanel,
  args: { experiment: EXPERIMENT, reaction: EXPERIMENT.model.reactions[0] },
  decorators: [
    (Story) => (
      <div className="w-[640px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ReactionSchemePanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** A step that already has a scheme. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByRole('img', { name: 'Chemical structure' })).toBeInTheDocument();
  },
};

/** A step with nothing drawn yet offers the sketcher and no image. */
export const Empty: Story = {
  args: { reaction: makeReaction({ rxnfile: undefined }) },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByRole('button', { name: 'Draw Structure' })).toBeInTheDocument();
  },
};

/** Without EDIT_EXPERIMENTS the frame is inert: the scheme is visible, the pencil is not usable. */
export const ReadOnly: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByRole('button', { name: 'Edit structure' })).toBeDisabled();
  },
};

/**
 * The whole point of the awaitable save: the sketcher stays open and its Save button spins
 * until the mutation lands, instead of closing on a write that has not happened yet.
 */
export const SavingHoldsTheDialogOpen: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  play: async ({ canvasElement }) => {
    await userEvent.click(await within(canvasElement).findByRole('button', { name: 'Edit structure' }));

    const save = await screen.findByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);

    // Still open, still busy, a full second before the mock responds.
    await waitFor(() => expect(save).toHaveAttribute('aria-busy', 'true'));
    await expect(save).toBeInTheDocument();
  },
};

/**
 * A failed save leaves the drawing where it is. `apiFetch` has already toasted the failure, so
 * the dialog says nothing itself — it just declines to close.
 */
export const FailedSaveKeepsTheDrawing: Story = {
  parameters: { msw: { handlers: failingMutateHandlers } },
  play: async ({ canvasElement }) => {
    await userEvent.click(await within(canvasElement).findByRole('button', { name: 'Edit structure' }));

    const save = await screen.findByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);

    // The spinner stops, but the sketcher is still there with the work in it.
    await waitFor(() => expect(save).not.toHaveAttribute('aria-busy', 'true'));
    await expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  },
};
