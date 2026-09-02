import { useInfiniteQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { getNextPageParam } from '@/lib/api/collections';

import type { Page } from '@/lib/types/common.ts';
import type { GlobalSearchRequest, GlobalSearchResult } from '@/lib/types/search.ts';

/** As in indigo-frontend's GlobalSearchLoader, which also asks for 20 at a time. */
const SEARCH_PAGE_SIZE = 20;

const searchKeys = {
  /** The request is the key; TanStack Query hashes it structurally, so the same criteria hit cache. */
  results: (request: GlobalSearchRequest) => ['globalSearch', request] as const,
};

function search(request: GlobalSearchRequest, pageNo: number, signal?: AbortSignal): Promise<Page<GlobalSearchResult>> {
  return apiFetch<Page<GlobalSearchResult>>(`/api/eln/search?pageNo=${pageNo}&pageSize=${SEARCH_PAGE_SIZE}`, {
    method: 'POST',
    body: JSON.stringify(request),
    signal,
  });
}

/**
 * Results for a submitted search. `null` until Search is pressed, which keeps the query
 * disabled — unlike the list screens there is nothing to debounce here, since this is
 * driven by a button rather than by typing.
 */
export function useGlobalSearch(request: GlobalSearchRequest | null) {
  return useInfiniteQuery({
    queryKey: searchKeys.results(request ?? {}),
    // Consuming `signal` lets an abandoned search abort instead of running to completion.
    queryFn: ({ pageParam, signal }) => search(request ?? {}, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: request !== null,
  });
}
