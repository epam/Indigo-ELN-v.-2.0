import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ReactionProductsTable } from '@/components/experiments/stoichiometry/products-table';
import { useExperiment } from '@/lib/api/experiments';
import { makeExperimentDetails, makeReaction, makeReactionOutput } from '@/mocks/fixtures';
import { failingMutateHandlers, handlers, slowMutateHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();

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
 * A second step, with product anchors of its own — the fixture list is shared, so reusing it
 * would put two rows carrying the same React key on screen at once with Show All Steps on.
 */
const TWO_STEPS = makeExperimentDetails({
  model: {
    significantFigures: 5,
    reactions: [
      makeReaction(),
      makeReaction({
        anchor: 'b0000000-0000-4000-8000-000000000002',
        outputs: [
          makeReactionOutput('f0000000-0000-4000-8000-000000000021', { outputName: 'P4', chemicalName: 'Anhydride' }),
        ],
      }),
    ],
  },
});

/**
 * Renders off the query cache rather than the prop, so a mutation's patch flows back into the
 * table the way it does in the app. Every story that asserts a payload uses it.
 */
function TableFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <ReactionProductsTable experiment={data} step={0} /> : null;
}

const meta = {
  title: 'Experiments/Stoichiometry/ReactionProductsTable',
  component: ReactionProductsTable,
  args: { experiment: EXPERIMENT, step: 0 },
  parameters: { layout: 'padded' },
} satisfies Meta<typeof ReactionProductsTable>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * Three products, and the fourth fixture row — the one with `intended: false` — deliberately
 * absent. That flag says the product was drawn in the reaction scheme; an unintended output
 * belongs to the Product Batch Summary.
 */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('columnheader', { name: 'Theo. Weight' })).toBeInTheDocument();
    await expect(canvas.getByRole('textbox', { name: 'Output Name, P0' })).toHaveValue('P0');
    await expect(canvas.getAllByRole('row')).toHaveLength(4); // header + three products
    await expect(canvas.queryByDisplayValue('P3')).not.toBeInTheDocument();
  },
};

/**
 * Products Type is the row's headline, so it carries colour: a column of identical grey triggers
 * would not read. All three values are reachable on an intended row — the type and the
 * `intended` flag are different things.
 */
export const ProductTypes: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Products Type, P0')).toHaveTextContent('Final');
    await expect(canvas.getByLabelText('Products Type, P1')).toHaveTextContent('Side');
    await expect(canvas.getByLabelText('Products Type, P2')).toHaveTextContent('Intermediate');
    // The colour is on the trigger, not in a StatusBadge — see `OutputTypeCell`.
    await expect(canvas.getByLabelText('Products Type, P0').className).toContain('border-green-200');
  },
};

/** Picking a type sends `SetOutputRowType` — note `outputType`, not `type`. */
export const PicksProductType: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Products Type, P0'));
    await userEvent.click(await within(await screen.findByRole('listbox')).findByRole('option', { name: 'Side' }));

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputRowType',
          anchor: 'f0000000-0000-4000-8000-000000000001',
          outputType: 'BY_PRODUCT',
        },
      ]),
    );
  },
};

/** EQ is the one editable number, and what the two theoretical columns are computed from. */
export const EditsEQ: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('EQ, P0');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '2{Enter}');

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'SetOutputRowEQ', anchor: 'f0000000-0000-4000-8000-000000000001', eq: '2' }]),
    );
  },
};

/**
 * The row's only action. `AddProductSample` is keyed by the **output** anchor — a sample anchor
 * resolves to nothing and the call 400s.
 */
export const AddsABatch: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Add batch to P1' }));

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'AddProductSample', anchor: 'f0000000-0000-4000-8000-000000000002' }]),
    );
  },
};

/**
 * Salt Code is editable only on a virtual compound, and Salt EQ needs a code **and** a virtual
 * compound — a stored one's salt EQ is registry data even when it has a code.
 */
export const SaltIsRegistryOwned: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // P1 is the virtual compound: both editable.
    await expect(canvas.getByLabelText('Salt Code, P1').tagName).toBe('BUTTON');
    await expect(canvas.getByLabelText('Salt EQ, P1')).toBeEnabled();
    // P2 is stored *and* carries a salt code — the case indigo-frontend let through. The two
    // cells lock differently: `DictionaryCell` swaps itself for plain text, while `NumericCell`
    // keeps its input and disables it, so there is no one query that covers both.
    const p2 = canvas.getByRole('textbox', { name: 'Output Name, P2' }).closest('tr')!;
    await expect(within(p2).queryByLabelText('Salt Code, P2')).not.toBeInTheDocument();
    await expect(within(p2).getByText('HCl')).toBeInTheDocument();
    await expect(within(p2).getByLabelText('Salt EQ, P2')).toBeDisabled();
  },
};

/**
 * No limiting reagent means no `theoMol`, which means no `theoWeight` — both cells show an
 * em-dash rather than a zero, since the server never computed them.
 */
export const NoTheoreticalValues: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const cell = canvas.getByLabelText('Theo. Weight, P2').closest('[data-slot="numeric-cell"]')!;
    await expect(cell.querySelector('[data-slot="numeric-cell-value"]')).toHaveTextContent('—');
  },
};

/** Search filters products, over their name, chemical name, formula and compound key. */
export const Search: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('searchbox', { name: 'Search products' }), 'acetic');

    await waitFor(async () => expect(canvas.getAllByRole('row')).toHaveLength(2)); // header + P1
    await expect(canvas.getByRole('textbox', { name: 'Output Name, P1' })).toBeInTheDocument();
  },
};

/** A term that matches nothing says so, rather than reading as an empty table. */
export const SearchMatchesNothing: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('searchbox', { name: 'Search products' }), 'zzz');
    await expect(await canvas.findByText('No product matches this search')).toBeInTheDocument();
  },
};

/**
 * With the toggle off the table shows one step; with it on, every step's products, and the
 * Reaction Step column is what tells them apart. It reads as a no-op today because nothing can
 * create a second step — this story makes one in the fixture.
 */
export const ShowAllSteps: Story = {
  args: { experiment: TWO_STEPS, step: 0 },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getAllByRole('row')).toHaveLength(4);

    await userEvent.click(canvas.getByRole('switch', { name: 'Show All Steps' }));

    await waitFor(async () => expect(canvas.getAllByRole('row')).toHaveLength(5));
    // The step of the row it belongs to, 1-based — not the hardcoded '1' indigo-frontend
    // returns. Scoped to P4's own row: '2' turns up in formula subscripts elsewhere.
    const p4 = canvas.getByRole('textbox', { name: 'Output Name, P4' }).closest('tr')!;
    await expect(within(p4).getByText('2')).toBeInTheDocument();
  },
};

/** A reaction whose scheme has no products drawn yet. */
export const Empty: Story = {
  args: {
    experiment: makeExperimentDetails({
      model: { significantFigures: 5, reactions: [makeReaction({ outputs: [] })] },
    }),
  },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByText('No product added')).toBeInTheDocument();
  },
};

/** Permission is one gate. Every control is disabled and the data is all still readable. */
export const ReadOnlyNoPermission: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Add batch to P0' })).toBeDisabled();
    await expect(canvas.getByLabelText('Products Type, P0')).toBeDisabled();
    // The data is all still there — read-only, not hidden. `TextCell` swaps its input for plain
    // text when it cannot be edited, so the name is no longer a textbox.
    await expect(canvas.queryByRole('textbox', { name: 'Output Name, P0' })).not.toBeInTheDocument();
    await expect(canvas.getByText('P0')).toBeInTheDocument();
  },
};

/** Status is the other gate: a signed experiment is read-only to a user who holds everything. */
export const ReadOnlySigned: Story = {
  args: { experiment: makeExperimentDetails({ status: 'SIGNED' }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Add batch to P0' })).toBeDisabled();
    await expect(canvas.getByLabelText('Products Type, P0')).toBeDisabled();
  },
};

/** The cell being saved freezes and floats a spinner; the rest of the table stays live. */
export const Saving: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: 'Add batch to P0' }));

    await waitFor(async () => expect(await screen.findByRole('status', { name: '' })).toHaveTextContent('Saving…'));
    // The neighbouring row's button is untouched — the overlay is scoped to one cell.
    await expect(canvas.getByRole('button', { name: 'Add batch to P1' })).toBeEnabled();
  },
};

/**
 * Nothing is optimistic. A rejected save leaves the cell showing what the server last confirmed;
 * `apiFetch` has already raised the toast, so there is nothing to handle here beyond clearing
 * the spinner.
 */
export const Failure: Story = {
  parameters: { msw: { handlers: failingMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const type = await canvas.findByLabelText('Products Type, P0');

    await userEvent.click(type);
    await userEvent.click(await within(await screen.findByRole('listbox')).findByRole('option', { name: 'Side' }));

    await waitFor(async () => expect(await canvas.findByLabelText('Products Type, P0')).toBeEnabled());
    await expect(canvas.getByLabelText('Products Type, P0')).toHaveTextContent('Final');
  },
};
