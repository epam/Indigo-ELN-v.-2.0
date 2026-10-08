import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { BatchDetailPanel } from '@/components/experiments/stoichiometry/batches/detail-panel';
import { useStoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { useExperiment } from '@/lib/api/experiments';
import { canEditExperiment } from '@/lib/types/experiments.ts';
import {
  DICTIONARIES,
  makeExperimentDetails,
  makeReactionOutput,
  makeReactionOutputSample,
  unknownCompound,
} from '@/mocks/fixtures';
import { handlers, slowMutateHandlers } from '@/mocks/handlers';

import type { BatchRow } from '@/components/experiments/stoichiometry/batches/columns';
import type { StoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();
/** Given a precursor here rather than in the fixture: only this panel renders the field. */
const REACTION = { ...EXPERIMENT.model.reactions[0], precursorReactantIds: ['STR-00000014-00'] };

function rowAt(outputIndex: number, sampleIndex: number): BatchRow {
  const output = REACTION.outputs[outputIndex];
  return { output, sample: output.samples[sampleIndex], step: 0 };
}

/** Batch 001 — one value in every field, including all five read-only composites. */
const POPULATED = rowAt(0, 0);
/** Batch 002 — nothing entered, the em-dash state of the whole panel. */
const EMPTY = rowAt(0, 1);
/** Batch 003 — `REGISTERED`, on a compound that carries a salt code and a salt EQ. */
const REGISTERED = rowAt(1, 0);

/** Nothing in flight and nothing to send: what the display-only stories pass. */
const IDLE: StoichiometryMutations = { save: async () => true, savingCells: new Set(), updatedNodes: new Map() };

/** Records the mutations that actually reached the wire, so a story can assert the payload. */
const sent: unknown[] = [];

const spyHandlers = [
  http.post('/api/eln/experiments/:id/mutate', async ({ request }) => {
    sent.push(await request.json());
    // A whole `MutationResponse`: one without `messages` rejects in `applyMutationResponse`,
    // which a dialog waiting on its save reads as a failure and stays open for.
    return HttpResponse.json({ patch: {}, unresolvedInputs: {}, messages: [], debugMessages: [] });
  }),
  ...handlers,
];

/**
 * The panel over the query cache and the table's real mutation hook, so a save goes out the way
 * it does in the app and its patch comes back the same way. Every story that asserts a payload
 * uses it; the rest take a fixed row and `IDLE`.
 */
function PanelFromCache({ outputIndex = 0, sampleIndex = 0 }: { outputIndex?: number; sampleIndex?: number }) {
  const { data } = useExperiment(EXPERIMENT.id);
  const mutations = useStoichiometryMutations(data ?? EXPERIMENT);

  if (!data) return null;

  const reaction = data.model.reactions[0];
  const output = reaction.outputs[outputIndex];

  return (
    <BatchDetailPanel
      row={{ output, sample: output.samples[sampleIndex], step: 0 }}
      reaction={reaction}
      canEdit={canEditExperiment(data)}
      mutations={mutations}
    />
  );
}

const meta = {
  title: 'Experiments/Stoichiometry/Batches/BatchDetailPanel',
  component: BatchDetailPanel,
  args: { row: POPULATED, reaction: REACTION, canEdit: true, mutations: IDLE },
  parameters: { layout: 'padded' },
} satisfies Meta<typeof BatchDetailPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The structure on the left, the batch's own fields on the right. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await expect(canvas.getByText(POPULATED.sample.nbkBatchNumber)).toBeInTheDocument();
    // Derived on the compound, shown without a box.
    await expect(canvas.getByText('180.16')).toBeInTheDocument();
    // Derived on the reaction, not on the batch.
    await expect(canvas.getByLabelText('Precursor/Reactant IDs')).toHaveValue('STR-00000014-00');
    // The STR code arrives on registration; this batch has not been registered.
    await expect(canvas.getByLabelText('Conversational Batch number')).toHaveValue('');
    // A derived quantity carries its unit into the read-only box.
    await expect(canvas.getByLabelText('Theo. Weight')).toHaveValue('500.4 mg');
    await expect(await canvas.findByAltText('Structure of batch 001')).toBeInTheDocument();
  },
};

/**
 * `calculatedBatchMF` arrives as HTML — `CompoundService.calculateBatchMF` builds it from
 * `MolFormula.toHTMLString()` — so the subscripts have to survive into the DOM rather than
 * being shown as tags.
 */
export const FormulaKeepsItsSubscripts: Story = {
  play: async ({ canvasElement }) => {
    const formula = within(canvasElement).getByText('Calculated Batch MF').nextElementSibling;
    await expect(formula).toHaveTextContent('C9H8O4');
    await expect(formula?.querySelectorAll('sub')).toHaveLength(3);
  },
};

/** Supporting material, so it starts folded away. */
export const AdditionalInformationIsCollapsed: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByLabelText('Compound State')).not.toBeInTheDocument();

    await userEvent.click(canvas.getByRole('button', { name: 'Additional Information' }));

    await expect(await canvas.findByLabelText('Compound State')).toBeInTheDocument();
    // The five composites, as their read-only summaries.
    await expect(canvas.getByLabelText('Melting Point')).toHaveValue('67 ~ 69 °C');
    await expect(canvas.getByLabelText('External Supplier')).toHaveValue('Sigma-Aldrich (A1234)');
    await expect(canvas.getByText('Toluene (1.2 eq)')).toBeInTheDocument();
    await expect(canvas.getByText('Water < 5 g/mL')).toBeInTheDocument();
    await expect(canvas.getByText('Ethanol: Soluble')).toBeInTheDocument();
    await expect(canvas.getByText('HPLC > 98')).toBeInTheDocument();
  },
};

/** A batch with nothing filled in. Every editable control is empty rather than missing. */
export const EmptyBatch: Story = {
  args: { row: EMPTY },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Batch Comment')).toHaveValue('');
    await expect(canvas.getByLabelText('Conversational Batch number')).toHaveValue('');
  },
};

/**
 * Registration freezes the **compound**, not the batch: `CompoundHandlers` rejects a registered
 * sample's salt code, salt EQ and stereoisomer, and nothing rejects the rest.
 */
export const RegisteredBatchFreezesCompoundFields: Story = {
  args: { row: REGISTERED },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Salt Code & Name')).toHaveAttribute('readonly');
    await expect(canvas.getByLabelText('Salt Equivalent')).toHaveAttribute('readonly');
    await expect(canvas.getByLabelText('Stereoisomer Code')).toHaveAttribute('readonly');
    // The batch's own fields stay live.
    await expect(canvas.getByLabelText('Source')).not.toHaveAttribute('readonly');
  },
};

/** A reader who cannot edit gets every value, and no control that pretends otherwise. */
export const ReadOnlyExperiment: Story = {
  args: { canEdit: false },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Source')).toHaveAttribute('readonly');
    await expect(canvas.getByLabelText('Batch Comment')).toHaveValue('Second attempt');
    // A multi-select becomes chips rather than an inert combobox.
    await userEvent.click(canvas.getByRole('button', { name: 'Additional Information' }));
    await expect(await canvas.findByText('Corrosive')).toBeInTheDocument();
  },
};

/** An unknown compound has no `compoundID`, so there is no picture to ask for. */
export const NoStructure: Story = {
  args: {
    row: {
      output: makeReactionOutput('f0000000-0000-4000-8000-00000000000e', {
        compound: unknownCompound(),
        samples: [],
      }),
      sample: makeReactionOutputSample('f1000000-0000-4000-8000-00000000000e'),
      step: 0,
    },
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('No structure available')).toBeInTheDocument();
    // The compound fields have nothing behind them, so they offer no control either.
    await expect(canvas.getByLabelText('Salt Code & Name')).toHaveAttribute('readonly');
  },
};

/** Picking is the commit gesture, exactly as it is for the table's cells. */
export const PicksSource: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache sampleIndex={1} />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Source'));
    // The popup is portalled, so it is reached with `screen`, not the canvas.
    // The row that clears the pick is blank to the eye, and still has a name.
    await expect(await screen.findByRole('option', { name: 'Clear selection' })).toHaveTextContent(/^\s*$/);
    const option = DICTIONARIES.SAMPLE_SOURCE![1];
    await userEvent.click(await screen.findByRole('option', { name: option.name }));

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetOutputSource', anchor: 'f1000000-0000-4000-8000-000000000002', source: option },
      ]),
    );
  },
};

/** Text saves when the field is left, and only when something actually changed. */
export const SavesBatchCommentOnBlur: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache sampleIndex={1} />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const comment = await canvas.findByLabelText('Batch Comment');
    await userEvent.type(comment, 'Repeat of 001');
    await userEvent.click(document.body);

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputBatchComment',
          anchor: 'f1000000-0000-4000-8000-000000000002',
          batchComment: 'Repeat of 001',
        },
      ]),
    );
  },
};

/** **Regression guard:** opening a field and leaving it unchanged must send nothing. */
export const NoRequestWhenUnchanged: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Batch Comment'));
    await userEvent.click(document.body);

    await userEvent.click(canvas.getByLabelText('Source'));
    await userEvent.keyboard('{Escape}');
    await userEvent.click(document.body);

    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(sent).toEqual([]);
  },
};

/**
 * Nothing to type into: the list ticks what is chosen and stays open, so several picks are one
 * visit — and one request, sent as the list is left.
 */
export const PicksSeveralHazards: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));
    const hazards = await canvas.findByLabelText('Health Hazards');
    await expect(hazards.tagName).toBe('BUTTON');

    const [held, added] = DICTIONARIES.HEALTH_HAZARD!;
    await userEvent.click(hazards);
    await expect(await screen.findByRole('option', { name: held.name })).toHaveAttribute('aria-selected', 'true');
    await userEvent.click(screen.getByRole('option', { name: added.name }));
    // Ticked, with the list still open, but not sent.
    await expect(screen.getByRole('listbox')).toBeInTheDocument();
    await expect(sent).toEqual([]);

    await userEvent.click(document.body);
    await waitFor(() => expect(sent).toHaveLength(1));
    await expect(sent[0]).toMatchObject({ type: 'SetOutputHealthHazards', healthHazards: [held, added] });
  },
};

/** Every list mutation is `@NotNull`, so clearing a list sends `[]`, never `null`. */
export const ClearingAListSendsEmpty: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));

    const hazard = DICTIONARIES.HEALTH_HAZARD![0];
    // A chosen row is ticked in the list, and picking it again is what removes it.
    await userEvent.click(await canvas.findByLabelText('Health Hazards'));
    await userEvent.click(await screen.findByRole('option', { name: hazard.name }));
    // The list stays open for the next pick, and leaving it is what sends. Not Escape, which abandons.
    await userEvent.click(document.body);
    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputHealthHazards',
          anchor: 'f1000000-0000-4000-8000-000000000001',
          healthHazards: [],
        },
      ]),
    );

    // The field freezes while its own save is in flight, so the next chip has to wait for it.
    const protection = DICTIONARIES.COMPOUND_PROTECTION![0];
    await waitFor(async () => expect(await canvas.findByLabelText('Compound Protection')).toBeEnabled());
    await userEvent.click(canvas.getByLabelText('Compound Protection'));
    await userEvent.click(await screen.findByRole('option', { name: protection.name }));
    await userEvent.click(document.body);

    await waitFor(() =>
      expect(sent.at(-1)).toEqual({
        type: 'SetOutputCompoundProtection',
        anchor: 'f1000000-0000-4000-8000-000000000001',
        compoundProtection: [],
      }),
    );
  },
};

/** A reader gets the composites' summaries and no pencil to open an editor with. */
export const ReadOnlyHasNoEditors: Story = {
  args: { canEdit: false },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Additional Information' }));

    await expect(await canvas.findByLabelText('Melting Point')).toHaveValue('67 ~ 69 °C');
    await expect(canvas.queryByRole('button', { name: /^Edit / })).not.toBeInTheDocument();
  },
};

/** A composite is edited in a dialog, and its Save is the commit. */
export const EditsMeltingPoint: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));
    await userEvent.click(await canvas.findByRole('button', { name: 'Edit Melting Point' }));

    const upper = await screen.findByLabelText('Upper, °C');
    await userEvent.clear(upper);
    await userEvent.type(upper, '70');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputMeltingPoint',
          anchor: 'f1000000-0000-4000-8000-000000000001',
          meltingPoint: { lower: 67, upper: 70 },
        },
      ]),
    );
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
  },
};

export const EditsResidualSolvents: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache sampleIndex={1} />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));
    await userEvent.click(await canvas.findByRole('button', { name: 'Edit Residual Solvents' }));

    const solvent = DICTIONARIES.SOLVENT![3];
    await userEvent.click(await screen.findByLabelText('Solvent Name, row 1'));
    await userEvent.click(await screen.findByRole('option', { name: solvent.name }));
    await userEvent.type(screen.getByLabelText('# EQ. of Solvent, row 1'), '2');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputResidualSolvents',
          anchor: 'f1000000-0000-4000-8000-000000000002',
          residualSolvents: [{ solvent, eq: 2 }],
        },
      ]),
    );
  },
};

export const EditsSolubility: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));
    await userEvent.click(await canvas.findByRole('button', { name: 'Edit Solubility in Solvents' }));

    // Batch 001 holds a quantitative row and a qualitative one; the first goes.
    await userEvent.click(await screen.findByRole('button', { name: 'Delete row 1' }));
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputSolubilityInSolvents',
          anchor: 'f1000000-0000-4000-8000-000000000001',
          solubilityInSolvents: [
            { type: 'QUALITATIVE', solvent: DICTIONARIES.SOLVENT![2], qualitativeType: 'SOLUBLE' },
          ],
        },
      ]),
    );
  },
};

/** The overlay is scoped to the field being saved; its neighbours stay live. */
export const Saving: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  render: () => <PanelFromCache sampleIndex={1} />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const comment = await canvas.findByLabelText('Batch Comment');
    await userEvent.type(comment, 'Repeat of 001');
    await userEvent.click(document.body);

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));
    // Frozen by the overlay's `inert`, which leaves no `disabled` attribute to look for.
    await expect(comment.closest('[inert]')).not.toBeNull();
    await expect(canvas.getByLabelText('Structure Comments').closest('[inert]')).toBeNull();
  },
};
