import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { BatchDetailPanel } from '@/components/experiments/stoichiometry/batches/detail-panel';
import { useStoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { useExperiment } from '@/lib/api/experiments';
import { canEditExperiment } from '@/lib/types/experiments.ts';
import { DICTIONARIES, makeExperimentDetails, makeReactionOutput, makeReactionOutputSample } from '@/mocks/fixtures';
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
/** Batch 003 — `REGISTERED`, on a virtual compound that carries a salt code and a salt EQ. */
const REGISTERED = rowAt(1, 0);

/** Nothing in flight and nothing to send: what the display-only stories pass. */
const IDLE: StoichiometryMutations = { save: () => {}, savingCells: new Set(), updatedNodes: new Map() };

/** Records the mutations that actually reached the wire, so a story can assert the payload. */
const sent: unknown[] = [];

const spyHandlers = [
  http.post('/api/eln/experiments/:id/mutate', async ({ request }) => {
    sent.push(await request.json());
    return HttpResponse.json({ patch: {} });
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
    // `STRCodeSample` is a string on the wire; indigo-frontend prints `[object Object]` here.
    await expect(canvas.getByLabelText('Conversational Batch number')).toHaveValue('STR-00000016-00-003');
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
    // The five composites the panel renders but cannot yet edit.
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

/** An `UNKNOWN` compound has no `compoundID`, so there is no picture to ask for. */
export const NoStructure: Story = {
  args: {
    row: {
      output: makeReactionOutput('f0000000-0000-4000-8000-00000000000e', {
        compound: { type: 'UNKNOWN', molWeight: {} },
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
 * The two ways an emptied list is spelled, which are **not** the same and are the easiest thing
 * on this panel to get wrong. `SetOutputHealthHazards` is `@NotNull`, so clearing it sends `[]`;
 * its three neighbours are `@Size(min = 1)` when present, so clearing one sends `null`.
 */
export const ClearingAListSendsNullOrEmpty: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <PanelFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Additional Information' }));

    const hazard = DICTIONARIES.HEALTH_HAZARD![0];
    await userEvent.click(await canvas.findByRole('button', { name: `Remove ${hazard.name}` }));
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
    await userEvent.click(await canvas.findByRole('button', { name: `Remove ${protection.name}` }));

    await waitFor(() =>
      expect(sent.at(-1)).toEqual({
        type: 'SetOutputCompoundProtection',
        anchor: 'f1000000-0000-4000-8000-000000000001',
        compoundProtection: null,
      }),
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
    await expect(comment).toBeDisabled();
    await expect(canvas.getByLabelText('Structure Comments')).not.toBeDisabled();
  },
};
