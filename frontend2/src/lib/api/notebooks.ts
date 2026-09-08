import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { collectionQueryParams, getNextPageParam, useSettledSearch } from '@/lib/api/collections';
import { useEntityAttachments, useUpdateEntityAccess } from '@/lib/api/entity-writes';
import { projectKeys } from '@/lib/api/projects';
import type { CollectionFilters, Page } from '@/lib/types/common.ts';
import type { Notebook, NotebookDetails, NotebookEditRequest, NotebookRequest } from '@/lib/types/notebooks.ts';

function fetchProjectNotebooks(
  projectId: string,
  filters: CollectionFilters,
  pageNo: number,
  signal?: AbortSignal,
): Promise<Page<Notebook>> {
  return apiFetch<Page<Notebook>>(
    `/api/eln/projects/${projectId}/notebooks?${collectionQueryParams(filters, pageNo)}`,
    { signal },
  );
}

/**
 * Lists are scoped by project id, so two projects' lists never share a cache entry.
 *
 * Details sit under their own `notebookDetails` root rather than beneath `notebooks`, for the
 * same reason spelled out in `projects.ts`: editing a notebook invalidates every list, and a
 * detail nested under that prefix would be caught by the same call and refetched immediately
 * after the mutation response had been written into it.
 *
 * Exported for the same reason `projectKeys` is: creating an experiment moves a notebook's
 * `experimentCount` and `experimentCountByStatus`, so `experiments.ts` invalidates `all` and
 * `detail` from there rather than writing those key arrays out a second time.
 */
export const notebookKeys = {
  all: () => ['notebooks'] as const,
  list: (projectId: string, filters: CollectionFilters) => ['notebooks', projectId, filters] as const,
  detail: (id: string) => ['notebookDetails', id] as const,
  // Its own root for the same reason the detail has one: creating a notebook consumes this
  // number, so it must not be swept up by — and refetched from — the list invalidation.
  nextNumber: () => ['notebookNextNumber'] as const,
};

/** Attachments and ACL, which every detail entity handles the same way. */
const NOTEBOOK_WRITES = { basePath: '/api/eln/notebooks', detailKey: notebookKeys.detail };

/** See `useProjects` — the debounce gates `enabled` so `isPending` covers the wait too. */
export function useProjectNotebooks(projectId: string, filters: CollectionFilters) {
  const settled = useSettledSearch(filters.search);

  return useInfiniteQuery({
    queryKey: notebookKeys.list(projectId, filters),
    queryFn: ({ pageParam, signal }) => fetchProjectNotebooks(projectId, filters, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: settled,
  });
}

function fetchNotebook(id: string, signal?: AbortSignal): Promise<NotebookDetails> {
  return apiFetch<NotebookDetails>(`/api/eln/notebooks/${id}`, { signal });
}

export function useNotebook(id: string) {
  return useQuery({
    queryKey: notebookKeys.detail(id),
    queryFn: ({ signal }) => fetchNotebook(id, signal),
  });
}

/** Pre-flight for the notebook_name_uq constraint, which has no friendly server message. */
export function checkNotebookNameExists(name: string): Promise<boolean> {
  return apiFetch<{ exists: boolean }>(`/api/eln/notebooks/existence?name=${encodeURIComponent(name)}`).then(
    (result) => result.exists,
  );
}

function editNotebook(id: string, request: NotebookEditRequest): Promise<NotebookDetails> {
  return apiFetch<NotebookDetails>(`/api/eln/notebooks/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

export function useEditNotebook(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: NotebookEditRequest) => editNotebook(id, request),
    onSuccess: (notebook) => {
      // The response is the whole notebook, so the detail needs no refetch — but the name is
      // on every card of the parent project's list, and those do.
      queryClient.setQueryData(notebookKeys.detail(id), notebook);
      void queryClient.invalidateQueries({ queryKey: notebookKeys.all() });
    },
  });
}

/**
 * The name a new notebook is seeded with: `max(name) + 1`, zero-padded to eight digits.
 *
 * Read as **text**. The endpoint returns a bare Java `String` through RESTEasy's string writer
 * rather than Jackson, so the body is `00000009` unquoted — which `JSON.parse` rejects outright,
 * a leading zero not being legal JSON. (`FeignUtil` decodes the same endpoint with a
 * `StringDecoder` ahead of its `JacksonDecoder`, for the same reason.)
 */
function fetchNextNotebookNumber(signal?: AbortSignal): Promise<string> {
  return apiFetch('/api/eln/notebooks/next-number', { responseType: 'text', signal });
}

/**
 * Enabled by the caller only while the Add Notebook dialog is open, and never cached: the number
 * is `max(name) + 1` across **all** projects, so one held from an earlier open has very likely
 * been taken since. `staleTime: 0` is what makes reopening the dialog ask again.
 *
 * The backend does not reserve it either — `NotebookService.getNextNotebookNumber` is explicitly
 * not race-safe — so this is a suggestion the user can overwrite, not an allocation.
 *
 * `retry: false`, against the client default of two retries on a 5xx, for that same reason. The
 * dialog is held inert while this is in flight, so a backoff chain would freeze the form for
 * seconds and toast each attempt, to spare the user typing eight digits they can see on the
 * project's notebook list. Failing once and opening on an empty editable field is the better
 * trade — and the field was always theirs to overwrite.
 */
export function useNextNotebookNumber(enabled: boolean) {
  return useQuery({
    queryKey: notebookKeys.nextNumber(),
    queryFn: ({ signal }) => fetchNextNotebookNumber(signal),
    enabled,
    staleTime: 0,
    retry: false,
  });
}

function createNotebook(projectId: string, request: NotebookRequest): Promise<NotebookDetails> {
  return apiFetch<NotebookDetails>(`/api/eln/projects/${projectId}/notebooks`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function useCreateNotebook(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: NotebookRequest) => createNotebook(projectId, request),
    onSuccess: () => {
      // A new notebook lands in every filtered list of this project, consumes the next number,
      // and moves two counts that live on the project rather than here: the header's
      // `notebookCount` and the notebook stat tile on /projects.
      void queryClient.invalidateQueries({ queryKey: notebookKeys.all() });
      void queryClient.invalidateQueries({ queryKey: notebookKeys.nextNumber() });
      void queryClient.invalidateQueries({ queryKey: projectKeys.detail(projectId) });
      void queryClient.invalidateQueries({ queryKey: projectKeys.totalCounts() });
    },
  });
}

/**
 * The notebook's half of `useEntityAttachments` and `useUpdateEntityAccess`. Both sub-resources
 * are identical to the project's bar the prefix, so everything but the target lives in
 * `entity-writes.ts`.
 */
export function useNotebookAttachments(id: string) {
  return useEntityAttachments<NotebookDetails>(NOTEBOOK_WRITES, id);
}

export function useUpdateNotebookAccess(id: string) {
  return useUpdateEntityAccess<NotebookDetails>(NOTEBOOK_WRITES, id);
}
