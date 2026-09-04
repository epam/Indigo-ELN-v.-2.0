import { expect, userEvent, waitFor, within } from 'storybook/test';

import { SampleResults } from '@/components/experiments/samples/sample-results';
import {
  emptySampleSearchHandlers,
  failingSampleSearchHandlers,
  loadingSampleSearchHandlers,
  pagedSampleHandlers,
} from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { FindSamplesRequest } from '@/lib/types/samples.ts';

/** What Analyze RXN asks: everything the catalogs hold containing the drawn structure. */
const REQUEST: FindSamplesRequest = {
  catalogs: ['ELN', 'PUBCHEM'],
  structure: { type: 'SUBSTRUCTURE', query: 'unresolved-molfile' },
};

const meta = {
  title: 'Experiments/Samples/SampleResults',
  component: SampleResults,
  args: {
    request: REQUEST,
    // The writes are stubbed out — the dialogs own those, and these stories are about how a
    // page of catalog hits renders.
    onAdd: () => {},
    addingRows: new Set<string>(),
    boundSamples: new Set<string>(),
    onCountChange: () => {},
    emptyMessage: 'No materials match this structure.',
  },
  decorators: [
    (Story) => (
      <div className="flex h-[420px] w-[860px] flex-col">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof SampleResults>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Hits from both catalogs. The PubChem rows have no id, so they offer no bookmark. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText('Acetylsalicylic acid')).toBeInTheDocument();
    // The formula arrives as HTML, so it has to render as subscripts rather than as escaped
    // tags. Not queried by text: `getByText` reads a node's own text, and `C<sub>9</sub>…` is
    // split across four of them.
    await expect(canvasElement.querySelector('td sub')).toBeInTheDocument();
    await expect(canvasElement).not.toHaveTextContent('<sub>');
  },
};

/**
 * The sheet is narrow, so the table must fit it rather than scroll: the two action columns are
 * the ones that would go over the right edge, and an Add button you have to scroll to find is
 * the thing `table-fixed` exists here to prevent.
 */
export const FitsItsWidthWithoutScrolling: Story = {
  decorators: [
    (Story) => (
      // The narrowest the sheet ever gets — its own 720px, less the dialog body's padding.
      <div className="flex h-[420px] w-[672px] flex-col">
        <Story />
      </div>
    ),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByText('Acetylsalicylic acid');

    const box = canvasElement.querySelector('div.overflow-y-auto');
    await expect(box).toBeInTheDocument();
    await expect(box!.scrollWidth).toBeLessThanOrEqual(box!.clientWidth);
    // A long name gives way instead, which is what makes that fit possible.
    await expect(canvas.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' })).toBeVisible();
  },
};

/** The chevron reveals the record and its structure. */
export const ExpandedRow: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(await canvas.findByRole('button', { name: 'Show details of Acetylsalicylic acid' }));
    await expect(await canvas.findByText('Nbk Batch Number')).toBeInTheDocument();
    await expect(await canvas.findByRole('img', { name: 'Structure of Acetylsalicylic acid' })).toBeInTheDocument();
  },
};

/** Marking writes through the endpoint and the row updates in place — no refetch. */
export const Marking: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const mark = await canvas.findByRole('button', { name: 'Add Acetylsalicylic acid to My Materials' });

    await userEvent.click(mark);

    await waitFor(() =>
      expect(canvas.getByRole('button', { name: 'Remove Acetylsalicylic acid from My Materials' })).toBeInTheDocument(),
    );
  },
};

/** A sample the step already holds cannot be added again; the button stays, explaining itself. */
export const AlreadyInStoichiometry: Story = {
  args: { boundSamples: new Set(['55555555-5555-4555-8555-000000000001']) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const add = await canvas.findByRole('button', { name: 'Acetylsalicylic acid is already in the stoichiometry' });
    await expect(add).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Add Salicylic acid to the stoichiometry' })).toBeEnabled();
  },
};

/**
 * Two hits per page, so the list has to walk the cursor to show everything — which is the only
 * place that round trip is exercised at all: `IntersectionObserver` does not exist in jsdom, so
 * no unit test can reach it.
 *
 * The sentinel sits below a short first page and is therefore already in view, which is exactly
 * how the real list behaves on a tall screen: it keeps fetching until the page is full.
 */
export const Paging: Story = {
  parameters: { msw: { handlers: pagedSampleHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByText('Acetylsalicylic acid');
    // The last fixture, three pages in.
    await waitFor(() => expect(canvas.getByText('2-acetyloxybenzoic acid')).toBeInTheDocument(), { timeout: 5_000 });
  },
};

/** Nothing in either catalog matches the drawn structure. */
export const Empty: Story = {
  parameters: { msw: { handlers: emptySampleSearchHandlers } },
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByText('No materials match this structure.')).toBeInTheDocument();
  },
};

/**
 * The search failed. `apiFetch` has already toasted why; this says which table is empty, because
 * the toast is gone in five seconds and the table is not.
 */
export const Failed: Story = {
  parameters: { msw: { handlers: failingSampleSearchHandlers } },
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByText(/Could not search the catalogs/)).toBeInTheDocument();
  },
};

/** Skeleton rows, and one live-region line for anyone not looking at them. */
export const Loading: Story = {
  parameters: { msw: { handlers: loadingSampleSearchHandlers } },
};
