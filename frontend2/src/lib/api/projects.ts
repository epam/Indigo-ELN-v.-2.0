import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { collectionQueryParams, getNextPageParam, SUGGEST_DEBOUNCE_MS, useSettledSearch } from '@/lib/api/collections';
import { useEntityAttachments, useUpdateEntityAccess } from '@/lib/api/entity-writes';
import { useSettled } from '@/lib/hooks/use-settled';
import type { CollectionFilters, Page, UUID } from '@/lib/types/common.ts';
import type { Project, ProjectDetails, ProjectEditRequest, ProjectRequest, TotalCounts } from '@/lib/types/projects.ts';

function fetchProjects(filters: CollectionFilters, pageNo: number, signal?: AbortSignal): Promise<Page<Project>> {
  return apiFetch<Page<Project>>(`/api/eln/projects?${collectionQueryParams(filters, pageNo)}`, {
    signal,
  });
}

function fetchTotalCounts(): Promise<TotalCounts> {
  return apiFetch<TotalCounts>('/api/eln/total-counts');
}

function createProject(request: ProjectRequest): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>('/api/eln/projects', { method: 'POST', json: request });
}

/**
 * Case-insensitive prefix match, distinct and capped at 20 by the backend. The term is
 * encoded because it is interpolated straight into a SQL LIKE, so a bare `%` or `_`
 * typed by the user would otherwise act as a wildcard.
 */
function suggestKeywords(search: string, signal?: AbortSignal): Promise<string[]> {
  return apiFetch<string[]>(`/api/eln/projects/keywords/suggest?search=${encodeURIComponent(search)}`, { signal });
}

/** Pre-flight for the project_name_uq constraint, which has no friendly server message. */
export async function checkProjectNameExists(name: string): Promise<boolean> {
  const result = await apiFetch<{ exists: boolean }>(`/api/eln/projects/existence?name=${encodeURIComponent(name)}`);
  return result.exists;
}

/**
 * Every query key this module issues, written out literally rather than composed from a shared
 * prefix — four short arrays are easier to read, and to check against `invalidateQueries`, than
 * spreads that have to be assembled in your head.
 *
 * Details sit under their own `projectDetails` root, not under `projects`. Creating or editing a
 * project invalidates every list, and a detail nested beneath that prefix would be caught by the
 * same call and refetched immediately after the mutation response had been written into it.
 *
 * Filters belong in the list key; the page number comes from pageParam.
 *
 * Exported although nothing outside this module *queries* on it: creating a notebook moves a
 * project's `notebookCount` and the notebook stat tile, so `notebooks.ts` invalidates `detail`
 * and `totalCounts` from there. The alternative — writing those two key arrays out a second
 * time — is how the two copies drift.
 */
export const projectKeys = {
  all: () => ['projects'] as const,
  list: (filters: CollectionFilters) => ['projects', filters] as const,
  detail: (id: UUID) => ['projectDetails', id] as const,
  totalCounts: () => ['totalCounts'] as const,
  keywordSuggestions: (search: string) => ['projectKeywords', search] as const,
};

/** Attachments and ACL, which every detail entity handles the same way. */
const PROJECT_WRITES = { basePath: '/api/eln/projects', detailKey: projectKeys.detail };

/**
 * Debounced by gating `enabled` while the key tracks the search term as typed, so
 * `isPending` spans both the wait and the request — InfiniteLoader then shows its skeletons
 * for the whole time rather than leaving the previous term's results up unannounced.
 * Only the search term is debounced; sort and createdByMe are discrete toggles that
 * should take effect at once, and leave `settled` alone. Clearing the box is a discrete
 * gesture too, which is why `useSettledSearch` lets an empty term through unwaited.
 */
export function useProjects(filters: CollectionFilters) {
  const settled = useSettledSearch(filters.search);

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

function fetchProject(id: UUID, signal?: AbortSignal): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>(`/api/eln/projects/${id}`, { signal });
}

export function useProject(id: UUID) {
  return useQuery({
    queryKey: projectKeys.detail(id),
    queryFn: ({ signal }) => fetchProject(id, signal),
  });
}

function editProject(id: UUID, request: ProjectEditRequest): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>(`/api/eln/projects/${id}`, {
    method: 'PATCH',
    json: request,
  });
}

export function useEditProject(id: UUID) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ProjectEditRequest) => editProject(id, request),
    onSuccess: (project) => {
      // The response is the whole project, so the detail needs no refetch — but the name and
      // keywords are on every list card, and those do.
      queryClient.setQueryData(projectKeys.detail(id), project);
      void queryClient.invalidateQueries({ queryKey: projectKeys.all() });
    },
  });
}

/**
 * The project's half of `useEntityAttachments`. The endpoints are identical bar the prefix, so
 * everything but the target lives in `entity-writes.ts`.
 */
export function useProjectAttachments(id: UUID) {
  return useEntityAttachments<ProjectDetails>(PROJECT_WRITES, id);
}

/** See `useUpdateEntityAccess` — only what changed is sent, and the response replaces `acl`. */
export function useUpdateProjectAccess(id: UUID) {
  return useUpdateEntityAccess<ProjectDetails>(PROJECT_WRITES, id);
}
