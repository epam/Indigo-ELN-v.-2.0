import { useState } from 'react';
import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { AddExperimentDialog } from '@/components/experiments/add-experiment-dialog';
import { TEMPLATES } from '@/mocks/fixtures';
import { createExperimentErrorHandlers, initializingTemplateHandlers, templatesErrorHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const NOTEBOOK_ID = '77777777-7777-7777-7777-777777777777';

function AddExperimentDialogHarness() {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <AddExperimentDialog open={open} onOpenChange={setOpen} notebookId={NOTEBOOK_ID} />
    </>
  );
}

const meta = {
  title: 'Experiments/AddExperimentDialog',
  component: AddExperimentDialogHarness,
} satisfies Meta<typeof AddExperimentDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The picker, once /templates has answered. Nothing is preselected. */
export const Default: Story = {
  play: async () => {
    await expect(await screen.findByRole('heading', { name: 'Add Experiment' })).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await expect(screen.getByLabelText(/Select Template/)).toHaveTextContent('Select Template');
  },
};

/**
 * The initializing phase, pinned on a template list that never arrives: the body is inert under a
 * spinner and Save is disabled, but Cancel still closes it — nothing has been chosen to lose.
 */
export const Initializing: Story = {
  parameters: { msw: { handlers: initializingTemplateHandlers } },
  play: async () => {
    await expect(await screen.findByRole('status')).toHaveTextContent('Loading…');
    await expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();
    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/** Save is unreachable until a template is chosen — the one field is required. */
export const SaveDisabledUntilTemplateChosen: Story = {
  play: async () => {
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();
    await userEvent.click(screen.getByLabelText(/Select Template/));
    await userEvent.click(await screen.findByRole('option', { name: TEMPLATES[1].name }));
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled());
  },
};

/** Creating closes the dialog and routes to the new experiment. */
export const CreatesAndCloses: Story = {
  play: async () => {
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await userEvent.click(screen.getByLabelText(/Select Template/));
    await userEvent.click(await screen.findByRole('option', { name: TEMPLATES[0].name }));
    const save = screen.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/**
 * A failed /templates needs no special case: the dialog opens, the popup says why it is empty,
 * and Save stays disabled because there is nothing to pick. The failure itself is toasted by
 * `apiFetch`.
 */
export const TemplatesFailToLoad: Story = {
  parameters: { msw: { handlers: templatesErrorHandlers } },
  play: async () => {
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await userEvent.click(screen.getByLabelText(/Select Template/));
    await expect(await screen.findByText('Could not load options')).toBeInTheDocument();
    await expect(screen.queryAllByRole('option')).toHaveLength(0);
    await expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();
  },
};

/** A failed POST leaves the dialog open with the chosen template still selected. */
export const CreateFails: Story = {
  parameters: { msw: { handlers: createExperimentErrorHandlers } },
  play: async () => {
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await userEvent.click(screen.getByLabelText(/Select Template/));
    await userEvent.click(await screen.findByRole('option', { name: TEMPLATES[0].name }));
    const save = screen.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);
    await waitFor(() => expect(screen.getByRole('heading', { name: 'Add Experiment' })).toBeInTheDocument());
    await expect(screen.getByLabelText(/Select Template/)).toHaveTextContent(TEMPLATES[0].name);
  },
};
