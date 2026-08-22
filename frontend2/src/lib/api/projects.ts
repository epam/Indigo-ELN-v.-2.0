import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { useSettled } from '@/lib/hooks/use-settled';
import type { Page } from '@/lib/types/common.ts';
import type { Project, ProjectDetails, ProjectFilters, ProjectRequest, TotalCounts } from '@/lib/types/projects.ts';

export const PROJECTS_PAGE_SIZE = 10;

/** Optional params are omitted when unset, matching indigo-frontend's client. */
export function projectsQueryString(filters: ProjectFilters, pageNo: number, pageSize = PROJECTS_PAGE_SIZE): string {
  const params = new URLSearchParams({
    sort: filters.sort,
    pageNo: String(pageNo),
    pageSize: String(pageSize),
  });
  if (filters.search) params.set('search', filters.search);
  if (filters.createdByMe) params.set('createdByMe', 'true');
  return params.toString();
}

export function fetchProjects(filters: ProjectFilters, pageNo: number, signal?: AbortSignal): Promise<Page<Project>> {
  return apiFetch<Page<Project>>(`projects?${projectsQueryString(filters, pageNo)}`, { signal });
}

export function fetchTotalCounts(): Promise<TotalCounts> {
  return apiFetch<TotalCounts>('total-counts');
}

export function createProject(request: ProjectRequest): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>('projects', { method: 'POST', body: JSON.stringify(request) });
}

/**
 * Case-insensitive prefix match, distinct and capped at 20 by the backend. The term is
 * encoded because it is interpolated straight into a SQL LIKE, so a bare `%` or `_`
 * typed by the user would otherwise act as a wildcard.
 */
export function suggestKeywords(search: string, signal?: AbortSignal): Promise<string[]> {
  return apiFetch<string[]>(`projects/keywords/suggest?search=${encodeURIComponent(search)}`, { signal });
}

/** Pre-flight for the project_name_uq constraint, which has no friendly server message. */
export function checkProjectNameExists(name: string): Promise<boolean> {
  return apiFetch<{ exists: boolean }>(`projects/existence?name=${encodeURIComponent(name)}`).then(
    (result) => result.exists,
  );
}

/** Filters belong in the key; the page number comes from pageParam. */
export const projectKeys = {
  all: () => ['projects'] as const,
  list: (filters: ProjectFilters) => ['projects', filters] as const,
  totalCounts: () => ['totalCounts'] as const,
  keywordSuggestions: (search: string) => ['projectKeywords', search] as const,
};

export function getNextPageParam(lastPage: Page<Project>): number | undefined {
  return lastPage.pageNo + 1 < lastPage.totalPages ? lastPage.pageNo + 1 : undefined;
}

export const SEARCH_DEBOUNCE_MS = 300;

/**
 * Debounced by gating `enabled` while the key tracks the search term as typed, so
 * `isPending` spans both the wait and the request — Collection then shows its skeletons
 * for the whole time rather than leaving the previous term's results up unannounced.
 * Only the search term is debounced; sort and createdByMe are discrete toggles that
 * should take effect at once, and leave `settled` alone.
 */
export function useProjects(filters: ProjectFilters) {
  const settled = useSettled(filters.search, SEARCH_DEBOUNCE_MS);

  return useInfiniteQuery({
    queryKey: projectKeys.list(filters),
    // Consuming `signal` lets an abandoned search abort instead of running to completion.
    queryFn: ({ pageParam, signal }) => fetchProjects(filters, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: settled,
  });
}

export function useTotalCounts() {
  return useQuery({
    queryKey: projectKeys.totalCounts(),
    queryFn: fetchTotalCounts,
  });
}

export const SUGGEST_DEBOUNCE_MS = 300;

/**
 * Keyword suggestions for the term as typed, debounced by holding `enabled` off until the
 * term stops changing rather than by lagging the term itself.
 *
 * That is what makes `isPending` a single honest "we don't know yet" signal: the key moves
 * on the first keystroke, so the status is pending through both the wait and the request.
 * `isLoading` would not do — it is `isPending && isFetching`, so it reads false while the
 * query sits disabled. Deliberately no `placeholderData`: holding the previous term's
 * matches would flip the status to success and show answers to a question no longer asked.
 */
export function useKeywordSuggestions(search: string) {
  const settled = useSettled(search, SUGGEST_DEBOUNCE_MS);

  return useQuery({
    queryKey: projectKeys.keywordSuggestions(search),
    // Consuming `signal` is what lets TanStack Query abort the request when the key moves
    // on and this query loses its observer (see Query#removeObserver).
    queryFn: ({ signal }) => suggestKeywords(search, signal),
    enabled: settled && search.length > 0,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createProject,
    onSuccess: () => {
      // A new project changes every filtered list and the stat tiles above them.
      void queryClient.invalidateQueries({ queryKey: projectKeys.all() });
      void queryClient.invalidateQueries({ queryKey: projectKeys.totalCounts() });
    },
  });
}
