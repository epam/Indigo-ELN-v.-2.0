import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { AnalyzeRxnDialog } from '@/components/experiments/analyze-rxn/analyze-rxn-dialog';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { emptySampleSearchHandlers, slowSampleWriteHandlers, uncountedSampleHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const BASE = makeExperimentDetails();

/**
 * The shared fixture gives both input rows the same formula, which would make two tabs here
 * read identically. The first row is salicylic acid everywhere else in that fixture, so this
 * gives it the formula it should have had — locally, rather than moving a fixture the
 * stoichiometry stories assert against.
 */
const REACTION = {
  ...BASE.model.reactions[0],
  inputs: BASE.model.reactions[0].inputs.map((input, index) =>
    index === 0
      ? { ...input, compound: { ...input.compound, formula: 'C<sub>7</sub>H<sub>6</sub>O<sub>3</sub>' } }
      : input,
  ),
};
const EXPERIMENT = { ...BASE, model: { ...BASE.model, reactions: [REACTION] } };
const [REACTANT, SOLVENT] = REACTION.inputs;

/** What a `SetScheme` reports when two of the drawn molecules matched no registered compound. */
const UNRESOLVED = {
  [REACTANT.anchor]: 'unresolved-reactant-molfile',
  [SOLVENT.anchor]: 'unresolved-solvent-molfile',
};

const meta = {
  title: 'Experiments/AnalyzeRxn/AnalyzeRxnDialog',
  component: AnalyzeRxnDialog,
  args: {
    open: true,
    onOpenChange: () => {},
    experiment: EXPERIMENT,
    reaction: REACTION,
    step: 0,
    unresolvedInputs: UNRESOLVED,
  },
} satisfies Meta<typeof AnalyzeRxnDialog>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * The dialog is portalled, so every query here goes through `screen` rather than the canvas.
 *
 * One tab per unmatched reactant, named after its formula, and the first tab's search has already
 * run — nothing was typed and nothing was pressed.
 */
export const Default: Story = {
  play: async () => {
    await expect(await screen.findByRole('tab', { name: /C7H6O3/ })).toBeInTheDocument();
    await expect(await screen.findByText('Acetylsalicylic acid')).toBeInTheDocument();
    // The count comes off the search, so it only appears once the catalogs have answered.
    await waitFor(() => expect(screen.getByRole('tab', { name: /\(3\)/ })).toBeInTheDocument());
  },
};

/**
 * Changing the catalog re-searches at once — no Search button, and no gesture beyond the radio.
 * PubChem hits have no ELN id, so their rows offer no bookmark.
 */
export const PubChemOnly: Story = {
  play: async () => {
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('radio', { name: 'PubChem' }));

    await waitFor(() => expect(screen.queryByText('Acetylsalicylic acid')).not.toBeInTheDocument());
    await expect(await screen.findByText('2-acetyloxybenzoic acid')).toBeInTheDocument();
    await expect(screen.queryByRole('button', { name: /My Materials/ })).not.toBeInTheDocument();
  },
};

/** The second tab searches its own structure, and only when it is opened. */
export const SecondTab: Story = {
  play: async () => {
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('tab', { name: /C4H6O3/ }));

    await expect(screen.getByRole('tab', { name: /C4H6O3/ })).toHaveAttribute('data-active');
    await expect(await screen.findByText('Acetylsalicylic acid')).toBeInTheDocument();
  },
};

/**
 * Adding binds the sample to the input row and leaves the dialog open — a reaction usually has
 * more than one unmatched reactant — and marks the tab resolved.
 *
 * The added row going disabled is not visible here: that comes from the mutation's patch landing
 * on the cached experiment detail, and this story renders the dialog with a prop rather than out
 * of a cache. `AlreadyInStoichiometry` in `sample-results.stories.tsx` covers the disabled state.
 */
export const AddingResolvesTheInput: Story = {
  play: async () => {
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' }));

    await waitFor(() => expect(screen.getByRole('tab', { name: /resolved/ })).toBeInTheDocument());
    // By name: Base UI gives a toast `role="dialog"` too, and one has just been raised.
    await expect(screen.getByRole('dialog', { name: 'Step 1: Analyze RXN' })).toBeInTheDocument();
  },
};

/**
 * The row's Add spinner, while the mutation is in flight. `SavingOverlay` inerts the button
 * rather than disabling it, and speaks through its own live region — so that is what this looks
 * for.
 */
export const AddingIsPending: Story = {
  parameters: { msw: { handlers: slowSampleWriteHandlers } },
  play: async () => {
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' }));

    await expect(await screen.findByText('Saving…')).toBeInTheDocument();
  },
};

/** One unmatched reactant only: a single tab, still labelled with its formula. */
export const OneUnresolvedInput: Story = {
  args: { unresolvedInputs: { [REACTANT.anchor]: 'unresolved-reactant-molfile' } },
  play: async () => {
    await expect(await screen.findAllByRole('tab')).toHaveLength(1);
  },
};

/**
 * PubChem reports no count, so a search that reached it comes back with `totalItems: null`. The
 * tab then says what it knows — how many have loaded — and marks it a lower bound. The `+` is
 * dropped once the cursor is spent, since what has loaded is the total by then.
 */
export const CountIsALowerBoundWhenUncounted: Story = {
  parameters: { msw: { handlers: uncountedSampleHandlers } },
  play: async () => {
    await expect(await screen.findByRole('tab', { name: /\(2\+\)/ })).toBeInTheDocument();
  },
};

/** Nothing in the catalogs matches what was drawn. */
export const NoMatches: Story = {
  parameters: { msw: { handlers: emptySampleSearchHandlers } },
  play: async () => {
    await expect(await screen.findByText('No materials match this structure.')).toBeInTheDocument();
  },
};
