import {useInfiniteQuery} from '@tanstack/react-query';

import {apiFetch} from '@/lib/api';
import {collectionQueryString, getNextPageParam, SEARCH_DEBOUNCE_MS} from '@/lib/api/collections';
import {useSettled} from '@/lib/hooks/use-settled';
import type {CollectionFilters, Page} from '@/lib/types/common.ts';
import type {Notebook} from '@/lib/types/notebooks.ts';

export const NOTEBOOKS_PAGE_SIZE = 10;

function fetchProjectNotebooks(
  projectId: string,
  filters: CollectionFilters,
  pageNo: number,
  signal?: AbortSignal,
): Promise<Page<Notebook>> {
  return apiFetch<Page<Notebook>>(
    `/api/eln/projects/${projectId}/notebooks?${collectionQueryString(filters, pageNo, NOTEBOOKS_PAGE_SIZE)}`,
    { signal },
  );
}

/** Scoped by project id, so two projects' lists never share a cache entry. */
const notebookKeys = {
  list: (projectId: string, filters: CollectionFilters) => ['notebooks', projectId, filters] as const,
};

/** See `useProjects` — the debounce gates `enabled` so `isPending` covers the wait too. */
export function useProjectNotebooks(projectId: string, filters: CollectionFilters) {
  const settled = useSettled(filters.search, SEARCH_DEBOUNCE_MS);

  return useInfiniteQuery({
    queryKey: notebookKeys.list(projectId, filters),
    queryFn: ({ pageParam, signal }) => fetchProjectNotebooks(projectId, filters, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: settled,
  });
}
