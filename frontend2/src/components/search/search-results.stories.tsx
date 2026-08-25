import { useInfiniteQuery } from '@tanstack/react-query';
import { expect, waitFor, within } from 'storybook/test';

import { SearchResults } from '@/components/search/search-results';
import { getNextPageParam, search } from '@/lib/api/search';
import { emptySearchHandlers, loadingSearchHandlers, searchErrorHandlers } from '@/mocks/handlers';

import type { GlobalSearchRequest } from '@/lib/types/search.ts';
import type { Meta, StoryObj } from '@storybook/react-vite';

const REQUEST: GlobalSearchRequest = { query: 'coupling' };

/** Runs the real query against the MSW handlers, so paging is genuinely exercised. */
function SearchResultsHarness() {
  const query = useInfiniteQuery({
    queryKey: ['globalSearch', REQUEST],
    queryFn: ({ pageParam, signal }) => search(REQUEST, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
  });

  return (
    <div className="w-[672px] p-4">
      <SearchResults query={query} onSelect={() => {}} />
    </div>
  );
}

const meta = {
  title: 'Search/SearchResults',
  component: SearchResultsHarness,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof SearchResultsHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** One of each entity type: only the experiment has a scheme, a status and a subject. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText('Search Results (27)')).toBeInTheDocument();

    // Project: both counts, no status.
    await expect(canvas.getByText('Kinase Inhibitor Screening')).toBeInTheDocument();
    await expect(canvas.getByText('Notebooks')).toBeInTheDocument();

    // Experiment: subject, status badge and a rendered scheme. The schemes each load on
    // their own query, so they arrive after the rows they sit in.
    await expect(canvas.getByText('Suzuki coupling of aryl bromide')).toBeInTheDocument();
    await expect((await canvas.findAllByAltText('Reaction scheme')).length).toBeGreaterThan(0);
  },
};

/** Role is a property of the match, so it shows only where the backend reported one. */
export const ShowsMatchedRole: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText('Role')).toBeInTheDocument();
    await expect(canvas.getByText('Reactant, Output')).toBeInTheDocument();
  },
};

/** Every result type links to its own detail page. */
export const LinksToDetailPages: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByText('Search Results (27)');

    // By href rather than by accessible name: a card's name is its whole text, and how the
    // header's two spans concatenate is the accname algorithm's business, not this test's.
    const hrefs = canvas.getAllByRole('link').map((link) => link.getAttribute('href'));
    await expect(hrefs).toContain('/projects/22222222-2222-4222-8222-222222222222');
    await expect(hrefs).toContain('/notebooks/33333333-3333-4333-8333-333333333333');
    await expect(hrefs).toContain('/experiments/11111111-1111-4111-8111-111111111111');
  },
};

/** Scrolling to the bottom pulls the second page in through Collection's sentinel. */
export const LoadsTheNextPage: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // The last row of page one — 20 of 27, and the sentinel sits below it.
    await waitFor(() => expect(canvas.getByText('00000001-0018')).toBeInTheDocument());

    // Re-scrolled on every attempt rather than once: the sentinel only fetches when the
    // observer fires, and a single scroll can land before the row it needs to reveal is
    // laid out. Scrolling again is a no-op once we are already at the bottom.
    await waitFor(
      () => {
        canvasElement.ownerDocument.documentElement.scrollTo({ top: 999_999 });
        expect(canvas.getByText('00000001-0025')).toBeInTheDocument();
      },
      { timeout: 10_000 },
    );
  },
};

export const Loading: Story = {
  parameters: { msw: { handlers: loadingSearchHandlers } },
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByText('Loading results…')).toBeInTheDocument();
  },
};

export const Empty: Story = {
  parameters: { msw: { handlers: emptySearchHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText('No results match these filters.')).toBeInTheDocument();
    // The count heading would only repeat what the empty message already said.
    await expect(canvas.queryByText(/Search Results/)).not.toBeInTheDocument();
  },
};

/** apiFetch toasts the failure; this explains the missing list. */
export const Failed: Story = {
  parameters: { msw: { handlers: searchErrorHandlers } },
  play: async ({ canvasElement }) => {
    await expect(await within(canvasElement).findByText(/Could not load results/)).toBeInTheDocument();
  },
};
