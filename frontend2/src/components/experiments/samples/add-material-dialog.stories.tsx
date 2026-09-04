import { useState } from 'react';
import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { AddMaterialDialog } from '@/components/experiments/samples/add-material-dialog';
import { Button } from '@/components/ui/button';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { emptySampleSearchHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];

const meta = {
  title: 'Experiments/Samples/AddMaterialDialog',
  component: AddMaterialDialog,
  args: {
    open: true,
    onOpenChange: () => {},
    experiment: EXPERIMENT,
    reaction: REACTION,
  },
} satisfies Meta<typeof AddMaterialDialog>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * Types a term and presses Search — Search is held until there is a criterion, so a story that
 * wants results has to give it one. The sheet is portalled, so `screen` throughout.
 */
async function search(term = 'aspirin') {
  await userEvent.type(await screen.findByRole('searchbox', { name: 'Quick search' }), term);
  await userEvent.click(screen.getByRole('button', { name: 'Search' }));
}

/**
 * The form as it opens: a quick-search box, the four catalogs, an empty sketcher, and Advanced
 * search collapsed. No search has run — there is no question yet, and Search says so.
 */
export const Default: Story = {
  play: async () => {
    await expect(await screen.findByRole('searchbox', { name: 'Quick search' })).toBeInTheDocument();
    await expect(screen.getByRole('radio', { name: 'All Catalogs' })).toBeChecked();
    // The structure-type radios belong to a drawing that does not exist yet.
    await expect(screen.queryByRole('radio', { name: 'Substructure' })).not.toBeInTheDocument();
    await expect(screen.queryByRole('table')).not.toBeInTheDocument();
    // A catalog on its own is not a search.
    await expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();
  },
};

/**
 * The sheet as the toolbar actually drives it — mounted shut, opened by the button, closed again.
 *
 * That is the whole reason `StoichiometryTable` renders it unconditionally: the panel slides in on
 * `data-[starting-style]` and out on `data-[ending-style]`, and Base UI can only run the second if
 * the element is still mounted to transition. Behind a `{open && …}` flag it vanishes instead.
 * Base UI unmounts the popup itself when the transition finishes, which is what this asserts.
 */
export const OpensAndCloses: Story = {
  render: (args) => {
    const [open, setOpen] = useState(false);
    return (
      <>
        <Button onClick={() => setOpen(true)}>Add sample</Button>
        <AddMaterialDialog {...args} open={open} onOpenChange={setOpen} />
      </>
    );
  },
  play: async () => {
    await expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    await userEvent.click(await screen.findByRole('button', { name: 'Add sample' }));
    await expect(await screen.findByRole('dialog')).toBeVisible();

    // Two controls are named Close — the header ✕ and the footer button, as on every sheet in
    // the app. Either dismisses; this takes the first, which is the ✕.
    const [dismiss] = screen.getAllByRole('button', { name: 'Close' });
    await userEvent.click(dismiss);

    // Gone only once the slide-out has finished; until then it is still there, transitioning.
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  },
};

/** Search brings back the catalog hits, with a count above them. */
export const Results: Story = {
  play: async () => {
    await search();

    await expect(await screen.findByText('Acetylsalicylic acid')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText(/materials found/)).toBeInTheDocument());
  },
};

/**
 * Advanced search under a catalog that reaches PubChem: nine filters unavailable, Molecular
 * Formula still offered, and a line saying why rather than nine greyed boxes to puzzle over.
 */
export const PubchemLimitsTheFilters: Story = {
  play: async () => {
    await userEvent.click(await screen.findByRole('button', { name: 'Advanced search' }));

    await expect(await screen.findByLabelText('Compound ID')).toBeDisabled();
    await expect(screen.getByLabelText('Molecular Formula')).toBeEnabled();
    await expect(screen.getByText(/PubChem does not support fine-grained search/)).toBeVisible();
  },
};

/** Choosing an ELN-only catalog hands the filters back, values intact. */
export const AdvancedSearchOnEln: Story = {
  play: async () => {
    await userEvent.click(await screen.findByRole('radio', { name: 'Indigo ELN' }));
    await userEvent.click(screen.getByRole('button', { name: 'Advanced search' }));

    const compoundId = await screen.findByLabelText('Compound ID');
    await expect(compoundId).toBeEnabled();
    await userEvent.type(compoundId, 'ASA');

    // Collapsing leaves the summary as the record of what is being searched for.
    await userEvent.click(screen.getByRole('button', { name: /Advanced search/ }));
    await waitFor(() => expect(screen.getByText('ASA')).toBeInTheDocument());
  },
};

/** Nothing in the catalogs matches. */
export const Empty: Story = {
  parameters: { msw: { handlers: emptySampleSearchHandlers } },
  play: async () => {
    await search();
    await expect(await screen.findByText('No materials match this search.')).toBeInTheDocument();
  },
};

/** Clear All empties the boxes, takes the results with them, and holds Search again. */
export const ClearAll: Story = {
  play: async () => {
    await search();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Clear All' }));

    await expect(screen.getByRole('searchbox', { name: 'Quick search' })).toHaveValue('');
    await waitFor(() => expect(screen.queryByText('Acetylsalicylic acid')).not.toBeInTheDocument());
    await expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();
  },
};
