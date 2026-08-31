import {useInfiniteQuery, useMutation, useQuery, useQueryClient} from '@tanstack/react-query';

import {apiFetch} from '@/lib/api';
import {collectionQueryString, getNextPageParam, SEARCH_DEBOUNCE_MS} from '@/lib/api/collections';
import {useSettled} from '@/lib/hooks/use-settled';
import type {Page} from '@/lib/types/common.ts';
import type {Experiment, ExperimentFilters} from '@/lib/types/experiments.ts';

export const EXPERIMENTS_PAGE_SIZE = 10;

/**
 * Exported although no component reads `marked`: `src/lib/query-client.ts` needs the hash to
 * decide what gets persisted to localStorage. Not a candidate for going private.
 */
export const experimentKeys = {
  marked: () => ['experiments', 'marked'] as const,
  notebookList: (notebookId: string, filters: ExperimentFilters) =>
    ['experiments', 'notebook', notebookId, filters] as const,
};

/** Unpaged: ExperimentAPI.getMarkedExperiments returns the full list. */
function fetchMarkedExperiments(): Promise<Experiment[]> {
  return apiFetch<Experiment[]>('/api/eln/experiments/marked');
}

export function useMarkedExperiments() {
  return useQuery({
    queryKey: experimentKeys.marked(),
    queryFn: fetchMarkedExperiments,
    // Persisted to localStorage, which drops any entry whose gcTime is shorter than the
    // persister's maxAge — the restored list must outlive the default five minutes.
    gcTime: Infinity,
  });
}

/**
 * The four params every collection takes, plus one repeatable `status` per selected status —
 * which `/notebooks/{id}/experiments` is the only endpoint to declare. Built on top of
 * `collectionQueryString` rather than inside it, so `collections.ts` stays the two-list
 * contract it documents itself as.
 */
function experimentsQueryString(filters: ExperimentFilters, pageNo: number, pageSize: number): string {
  const params = new URLSearchParams(collectionQueryString(filters, pageNo, pageSize));
  for (const status of filters.statuses) params.append('status', status);
  return params.toString();
}

function fetchNotebookExperiments(
  notebookId: string,
  filters: ExperimentFilters,
  pageNo: number,
  signal?: AbortSignal,
): Promise<Page<Experiment>> {
  return apiFetch<Page<Experiment>>(
    `/api/eln/notebooks/${notebookId}/experiments?${experimentsQueryString(filters, pageNo, EXPERIMENTS_PAGE_SIZE)}`,
    { signal },
  );
}

/** See `useProjects` — the debounce gates `enabled` so `isPending` covers the wait too. */
export function useNotebookExperiments(notebookId: string, filters: ExperimentFilters) {
  const settled = useSettled(filters.search, SEARCH_DEBOUNCE_MS);

  return useInfiniteQuery({
    queryKey: experimentKeys.notebookList(notebookId, filters),
    queryFn: ({ pageParam, signal }) => fetchNotebookExperiments(notebookId, filters, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: settled,
  });
}

function setMarked(id: string, marked: boolean): Promise<boolean> {
  return apiFetch<boolean>(`/api/eln/experiments/${id}/${marked ? 'mark' : 'unmark'}`, { method: 'POST' });
}

/**
 * Stars or unstars an experiment. Both the sidebar's starred list and every experiment list
 * carry `marked`, and neither response says what the other should now hold, so both are
 * invalidated rather than patched.
 */
export function useToggleMark() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, marked }: { id: string; marked: boolean }) => setMarked(id, marked),
    // One prefix: `experimentKeys.marked()` and `notebookList()` both start with 'experiments'.
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['experiments'] }),
  });
}

/**
 * Where to fetch an experiment's reaction scheme. `revision` is a cache-buster only —
 * ExperimentResource ignores it, and the endpoint declares a 30-day cache — so putting it
 * in the path is also what keeps the client-side cache entry correct.
 */
export function experimentPicturePath(id: string, revision: number | null): string {
  const query = revision === null ? '' : `?revision=${revision}`;
  return `/api/eln/experiments/${id}/picture${query}`;
}
