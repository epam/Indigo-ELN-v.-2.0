import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ProductBatchSummaryTable } from '@/components/experiments/stoichiometry/batches-table';
import { useExperiment } from '@/lib/api/experiments';
import { makeExperimentDetails, makeReaction } from '@/mocks/fixtures';
import { failingMutateHandlers, handlers, slowMutateHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];

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
 * Renders off the query cache rather than the prop, so a mutation's patch flows back into the
 * table the way it does in the app. Every story that asserts a payload uses it.
 */
function TableFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <ProductBatchSummaryTable experiment={data} reaction={data.model.reactions[0]} step={0} /> : null;
}

const meta = {
  title: 'Experiments/Stoichiometry/ProductBatchSummaryTable',
  component: ProductBatchSummaryTable,
  args: { experiment: EXPERIMENT, reaction: REACTION, step: 0 },
  parameters: { layout: 'padded' },
} satisfies Meta<typeof ProductBatchSummaryTable>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * Five batches across four products — including P3's, whose product is `intended: false`. That
 * row is the reason this table exists: the products table filters it out, and a batch of
 * something the reaction merely threw off has nowhere else to live.
 */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('columnheader', { name: 'Total Weight' })).toBeInTheDocument();
    await expect(canvas.getAllByRole('row')).toHaveLength(6); // header + five batches
    // The server's zero-padded short form, not the full notebook batch number.
    await expect(canvas.getByText('001')).toBeInTheDocument();
    await expect(canvas.getByText('005')).toBeInTheDocument();
  },
};

/** The product type is a static pill here — the products table is where it is edited. */
export const ProductTypeIsReadOnly: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getAllByText('Final').length).toBeGreaterThan(0);
    await expect(canvas.getByText('Intermediate')).toBeInTheDocument();
    await expect(canvas.queryByLabelText('Products Type, batch 001')).not.toBeInTheDocument();
  },
};

/** One of the three editable numbers. Value and unit are sent together or not at all. */
export const EditsTotalWeight: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    const input = await canvas.findByLabelText('Total Weight, batch 001');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '500{Enter}');

    await waitFor(() =>
      expect(sent).toEqual([
        {
          type: 'SetOutputActualWeight',
          anchor: 'f1000000-0000-4000-8000-000000000001',
          actualWeight: '500',
          unit: 'MG',
        },
      ]),
    );
  },
};

/** Purity is an input to every other formula on the row, never an output of one. */
export const EditsPurity: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    // A new batch starts at 100 % purity, so the cell has to be cleared before it is retyped.
    const input = await canvas.findByLabelText('Purity, batch 002');
    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '95{Enter}');

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'SetOutputPurity', anchor: 'f1000000-0000-4000-8000-000000000002', purity: '95' }]),
    );
  },
};

/** Molarity and Yield are computed by the backend, so neither offers an input. */
export const CalculatedColumnsAreReadOnly: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Molarity, batch 001')).toBeDisabled();
    await expect(canvas.getByLabelText('Yield, batch 001')).toBeDisabled();
    await expect(canvas.getByLabelText('Total Weight, batch 001')).toBeEnabled();
  },
};

/**
 * Sync is the one action keyed by the **output** anchor, and the only row it is offered on is the
 * one whose product was never drawn in the scheme.
 */
export const SyncsWithProducts: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await expect(
      await canvas.findByRole('button', { name: 'P0 is already synced with Products (batch 001)' }),
    ).toBeDisabled();
    await userEvent.click(canvas.getByRole('button', { name: 'Sync P3 with Products (batch 005)' }));

    await waitFor(() =>
      expect(sent).toEqual([
        { type: 'SetOutputRowIntended', anchor: 'f0000000-0000-4000-8000-000000000004', intended: true },
      ]),
    );
  },
};

/** Registration sends the batch to the compound registry. */
export const RegistersBatch: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Register batch 001' }));

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'RegisterSample', anchor: 'f1000000-0000-4000-8000-000000000001' }]),
    );
  },
};

/**
 * A registered batch is a registry record: Register and Delete lock, and say why. Its **numbers
 * do not** — the backend rejects a registered sample's compound mutations only, so disabling the
 * numeric cells would forbid an edit it accepts.
 */
export const RegisteredBatchIsProtected: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Batch 003 is already registered' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Delete batch 003' })).toBeDisabled();
    await expect(canvas.getByLabelText('Total Weight, batch 003')).toBeEnabled();
  },
};

/**
 * A failed registration is meant to be retried, so it is deliberately *not* protected — and the
 * reason it failed is surfaced on the status cell, which indigo-frontend never did.
 */
export const FailedRegistrationCanBeRetried: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Register batch 004' })).toBeEnabled();
    await expect(canvas.getByRole('button', { name: 'Delete batch 004' })).toBeEnabled();
    await expect(canvas.getByText('Failed')).toHaveAttribute('title', 'Compound registry rejected the structure');
  },
};

/** Deleting a batch leaves its product row alone — there is no mutation that removes one. */
export const DeletesBatch: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Delete batch 002' }));

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'RemoveProductSample', anchor: 'f1000000-0000-4000-8000-000000000002' }]),
    );
  },
};

/** `AddNoProductSample` is keyed by the **reaction** anchor, and creates the product too. */
export const AddsEmptyBatch: Story = {
  parameters: { msw: { handlers: spyHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByRole('button', { name: 'Add empty batch' }));

    await waitFor(() =>
      expect(sent).toEqual([{ type: 'AddNoProductSample', anchor: 'b0000000-0000-4000-8000-000000000001' }]),
    );
  },
};

/**
 * The chevron opens `BatchDetailPanel`, which has its own stories — this only pins that the row
 * expands the right batch and that the panel is closed until asked for.
 */
export const ExpandsDetail: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // Closed by default — a detail panel is supporting material, not the point of the table.
    await expect(canvas.queryByText(/^Notebook Batch #:/)).not.toBeInTheDocument();

    await userEvent.click(canvas.getByRole('button', { name: 'Show details of batch 001' }));

    await expect(await canvas.findByText('20260101-0001-001')).toBeInTheDocument();
    await expect(canvas.getByRole('button', { name: 'Hide details of batch 001' })).toBeInTheDocument();
  },
};

/** Search covers the batch number, the product name, the compound key and the status. */
export const Search: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('searchbox', { name: 'Search batches' }), 'registered');

    await waitFor(async () => expect(canvas.getAllByRole('row')).toHaveLength(2)); // header + batch 003
    await expect(canvas.getByText('003')).toBeInTheDocument();
  },
};

/** A term that matches nothing says so, rather than reading as an empty table. */
export const SearchMatchesNothing: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByRole('searchbox', { name: 'Search batches' }), 'zzz');
    await expect(await canvas.findByText('No batch matches this search')).toBeInTheDocument();
  },
};

/** A reaction whose products have no batches yet. */
export const Empty: Story = {
  args: { reaction: makeReaction({ outputs: [] }) },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByText('No batch added')).toBeInTheDocument();
  },
};

/** Permission is one gate. Every control is disabled and the data is all still readable. */
export const ReadOnlyNoPermission: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Add empty batch' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Register batch 001' })).toBeDisabled();
    await expect(canvas.getByLabelText('Total Weight, batch 001')).toBeDisabled();
    // Exporting changes nothing, so it stays available without edit rights.
    await expect(canvas.getByRole('button', { name: 'Export SDF' })).toBeEnabled();
    await expect(canvas.getByText('001')).toBeInTheDocument();
  },
};

/** Status is the other gate: a signed experiment is read-only to a user who holds everything. */
export const ReadOnlySigned: Story = {
  args: { experiment: makeExperimentDetails({ status: 'SIGNED' }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Import SDF' })).toBeDisabled();
    await expect(canvas.getByLabelText('Purity, batch 001')).toBeDisabled();
  },
};

/** The cell being saved freezes and floats a spinner; the rest of the table stays live. */
export const Saving: Story = {
  parameters: { msw: { handlers: slowMutateHandlers } },
  render: () => <TableFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: 'Delete batch 002' }));

    await waitFor(async () => expect(await screen.findByRole('status', { name: '' })).toHaveTextContent('Saving…'));
    // The neighbouring row's button is untouched — the overlay is scoped to one cell.
    await expect(canvas.getByRole('button', { name: 'Delete batch 001' })).toBeEnabled();
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
    const input = await canvas.findByLabelText('Purity, batch 002');

    await userEvent.click(input);
    await userEvent.clear(input);
    await userEvent.type(input, '95{Enter}');

    await waitFor(async () => expect(await canvas.findByLabelText('Purity, batch 002')).toBeEnabled());
    const cell = canvas.getByLabelText('Purity, batch 002').closest('[data-slot="numeric-cell"]')!;
    await expect(cell.querySelector('[data-slot="numeric-cell-value"]')).toHaveTextContent('100');
  },
};

/** Column show/hide has nowhere to live server-side, so the gear is inert. */
export const TableSettingsAreInert: Story = {
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByRole('button', { name: 'Table settings' })).toBeDisabled();
  },
};
