import type { CollectionFilters, Page } from '@/lib/types/common.ts';

/**
 * The query string shared by every paged entity list. `/projects` and
 * `/projects/{id}/notebooks` take the same four params, so they build them the same way.
 *
 * Optional params are omitted when unset, matching indigo-frontend's client. `pageSize` is
 * required rather than defaulted: the two lists happen to agree on 10 today, and a shared
 * default would quietly tie them together.
 */
export function collectionQueryString(filters: CollectionFilters, pageNo: number, pageSize: number): string {
  const params = new URLSearchParams({
    sort: filters.sort,
    pageNo: String(pageNo),
    pageSize: String(pageSize),
  });
  if (filters.search) params.set('search', filters.search);
  if (filters.createdByMe) params.set('createdByMe', 'true');
  return params.toString();
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
export const SEARCH_DEBOUNCE_MS = 300;

/**
 * How long a typeahead has to settle before its lookup runs — the suggestion endpoints'
 * counterpart to `SEARCH_DEBOUNCE_MS`, shared by keywords, users and experiment references so the
 * three do not drift.
 */
export const SUGGEST_DEBOUNCE_MS = 300;
