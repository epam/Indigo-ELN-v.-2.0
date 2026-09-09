import { useSettled } from '@/lib/hooks/use-settled';
import type { CollectionFilters, Page } from '@/lib/types/common.ts';

/**
 * How many rows a paged entity list asks for. One number for all three lists, which is also
 * what each of them draws first-load skeletons for.
 */
export const COLLECTION_PAGE_SIZE = 10;

/**
 * The query params shared by every paged entity list. `/projects` and
 * `/projects/{id}/notebooks` take the same four, so they build them the same way; the
 * experiments list appends its own to what this returns.
 *
 * Optional params are omitted when unset, matching indigo-frontend's client.
 */
export function collectionQueryParams(filters: CollectionFilters, pageNo: number): URLSearchParams {
  const params = new URLSearchParams({
    sort: filters.sort,
    pageNo: String(pageNo),
    pageSize: String(COLLECTION_PAGE_SIZE),
  });
  if (filters.search) params.set('search', filters.search);
  if (filters.createdByMe) params.set('createdByMe', 'true');
  return params;
}

/** Pages are zero-based, so the last one is `totalPages - 1` and has no successor. */
export function getNextPageParam<T>(lastPage: Page<T>): number | undefined {
  return lastPage.pageNo + 1 < lastPage.totalPages ? lastPage.pageNo + 1 : undefined;
}

/**
 * How long a list search box has to settle before its query runs. Lives here rather than on one
 * of the lists, so neither has to import it from the other.
 *
 * The debounce gates `enabled` while the key tracks the term as typed — see `useProjects`.
 */
const SEARCH_DEBOUNCE_MS = 300;

/**
 * Whether the list's search term is ready to query on. An empty term is a discrete gesture —
 * the box's native clear button, or select-all-delete — so there is nothing left to wait for,
 * and the unfiltered list should come straight back instead of sitting behind skeletons for
 * the debounce. Anything typed settles the usual way.
 */
export function useSettledSearch(search: string): boolean {
  return useSettled(search, SEARCH_DEBOUNCE_MS) || search === '';
}

/**
 * How long a typeahead has to settle before its lookup runs — the suggestion endpoints'
 * counterpart to `SEARCH_DEBOUNCE_MS`, shared by keywords, users and experiment references so the
 * three do not drift.
 */
export const SUGGEST_DEBOUNCE_MS = 300;
