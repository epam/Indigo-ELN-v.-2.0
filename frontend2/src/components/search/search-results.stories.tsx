import { http, HttpResponse } from 'msw';
import { expect, waitFor, within } from 'storybook/test';

import { SearchResults } from '@/components/search/search-results';
import { useGlobalSearch } from '@/lib/api/search';
import { SEARCH_RESULTS } from '@/mocks/fixtures';
import { emptySearchHandlers, loadingSearchHandlers, searchErrorHandlers } from '@/mocks/handlers';

import type { GlobalSearchRequest } from '@/lib/types/search.ts';
import type { Meta, StoryObj } from '@storybook/react-vite';

const REQUEST: GlobalSearchRequest = { query: 'coupling' };

/** Runs the app's own query against the MSW handlers, so paging is genuinely exercised. */
function SearchResultsHarness() {
  const query = useGlobalSearch(REQUEST);

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

const SHORT_PAGE_SIZE = 3;

/**
 * Pages short enough that the sentinel is still on screen once the first one has rendered, so
 * the second is pulled in with no scrolling at all.
 *
 * The default fixture is 27 results at 20 a page, which buries the sentinel far below the fold
 * and forces a story to scroll for it. That version of this test was the least stable thing in
 * the suite: it hung once at its own ten-second budget, in a run where every other measurement
 * of it sat between 90 ms and 624 ms — idle, inside a full suite, and with all sixteen cores
 * saturated. Whatever stalled it, the scroll was not what it was there to prove.
 */
const shortPageHandlers = [
  http.post(`/api/eln/search`, ({ request }) => {
    const pageNo = Number(new URL(request.url).searchParams.get('pageNo') ?? 0);
    const page = SEARCH_RESULTS.slice(pageNo * SHORT_PAGE_SIZE, (pageNo + 1) * SHORT_PAGE_SIZE);
    return HttpResponse.json({
      pageNo,
      pageSize: SHORT_PAGE_SIZE,
      totalItems: SEARCH_RESULTS.length,
      totalPages: Math.ceil(SEARCH_RESULTS.length / SHORT_PAGE_SIZE),
      items: page,
    });
  }),
];

/**
 * The sentinel pulls the second page in and its rows append below the first page's.
 *
 * What this no longer covers is a scroll bringing the sentinel into view — that part is
 * `IntersectionObserver`'s own behaviour rather than anything here. What it still covers is the
 * part that is ours: that the sentinel sits *below* the list, that reaching it fetches, and that
 * the new page appends rather than replacing.
 */
export const LoadsTheNextPage: Story = {
  parameters: { msw: { handlers: shortPageHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const firstPageLastRow = SEARCH_RESULTS[SHORT_PAGE_SIZE - 1].name;
    const secondPageFirstRow = SEARCH_RESULTS[SHORT_PAGE_SIZE].name;

    await waitFor(() => expect(canvas.getByText(firstPageLastRow)).toBeInTheDocument());
    await waitFor(() => expect(canvas.getByText(secondPageFirstRow)).toBeInTheDocument());
    // Appended, not swapped in.
    await expect(canvas.getByText(firstPageLastRow)).toBeInTheDocument();
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
