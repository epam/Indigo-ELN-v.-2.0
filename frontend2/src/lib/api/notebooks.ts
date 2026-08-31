import {useInfiniteQuery, useMutation, useQuery, useQueryClient} from '@tanstack/react-query';

import {apiDownload, apiFetch} from '@/lib/api';
import {collectionQueryString, getNextPageParam, SEARCH_DEBOUNCE_MS} from '@/lib/api/collections';
import {useSettled} from '@/lib/hooks/use-settled';
import type {AccessForm, ACLEntry, Attachment, CollectionFilters, Page} from '@/lib/types/common.ts';
import type {Notebook, NotebookDetails, NotebookEditRequest} from '@/lib/types/notebooks.ts';

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

/**
 * Lists are scoped by project id, so two projects' lists never share a cache entry.
 *
 * Details sit under their own `notebookDetails` root rather than beneath `notebooks`, for the
 * same reason spelled out in `projects.ts`: editing a notebook invalidates every list, and a
 * detail nested under that prefix would be caught by the same call and refetched immediately
 * after the mutation response had been written into it.
 */
const notebookKeys = {
  all: () => ['notebooks'] as const,
  list: (projectId: string, filters: CollectionFilters) => ['notebooks', projectId, filters] as const,
  detail: (id: string) => ['notebookDetails', id] as const,
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

/** Returns the notebook's full attachment list, not just the new entries. */
function uploadNotebookAttachment(id: string, file: File): Promise<Attachment[]> {
  const body = new FormData();
  body.append('file', file, file.name);
  return apiFetch<Attachment[]>(`/api/eln/notebooks/${id}/attachments`, { method: 'POST', body });
}

function deleteNotebookAttachment(id: string, attachmentId: string): Promise<void> {
  return apiFetch<void>(`/api/eln/notebooks/${id}/attachments/${attachmentId}`, { method: 'DELETE' });
}

/**
 * Saves the attachment to disk. The endpoint sets `Content-Disposition` from the same name the
 * DTO carries, so the fallback matters only if that header is ever stripped in transit.
 */
function downloadNotebookAttachment(id: string, attachmentId: string, fallbackFilename: string): Promise<void> {
  return apiDownload(`/api/eln/notebooks/${id}/attachments/${attachmentId}`, fallbackFilename);
}

/** Patches one field of the cached detail, leaving the rest of the notebook untouched. */
function patchNotebookDetails(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  patch: (notebook: NotebookDetails) => NotebookDetails,
) {
  queryClient.setQueryData<NotebookDetails>(notebookKeys.detail(id), (notebook) =>
    notebook ? patch(notebook) : notebook,
  );
}

/**
 * Files are uploaded one at a time: the endpoint takes a single `file` part, and each response
 * carries the full list, so a parallel upload would race and the last response home would drop
 * the others. `attachments` is read from the final response rather than accumulated.
 */
function useUploadNotebookAttachments(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (files: File[]) => {
      let attachments: Attachment[] = [];
      for (const file of files) {
        attachments = await uploadNotebookAttachment(id, file);
      }
      return attachments;
    },
    onSuccess: (attachments) => patchNotebookDetails(queryClient, id, (notebook) => ({ ...notebook, attachments })),
  });
}

function useDeleteNotebookAttachment(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (attachmentId: string) => deleteNotebookAttachment(id, attachmentId),
    onSuccess: (_result, attachmentId) =>
      patchNotebookDetails(queryClient, id, (notebook) => ({
        ...notebook,
        attachments: notebook.attachments.filter((attachment) => attachment.id !== attachmentId),
      })),
  });
}

/**
 * The three calls `AttachmentList` needs, bound to one notebook — the notebook half of
 * `useProjectAttachments`. The endpoints are identical bar the prefix.
 */
export function useNotebookAttachments(id: string) {
  const upload = useUploadNotebookAttachments(id);
  const remove = useDeleteNotebookAttachment(id);

  return {
    upload,
    remove,
    download: (attachment: Attachment) => downloadNotebookAttachment(id, attachment.id, attachment.name),
  };
}

function updateNotebookAccess(id: string, updates: AccessForm[]): Promise<ACLEntry[]> {
  return apiFetch<ACLEntry[]>(`/api/eln/notebooks/${id}/access`, {
    method: 'POST',
    body: JSON.stringify(updates),
  });
}

/**
 * `ACLService.updateNotebookACL` upserts entry by entry, so only what changed needs sending.
 * The response is the recomputed ACL for the whole notebook — inherited entries included — so
 * it replaces `acl` wholesale rather than being merged in.
 */
export function useUpdateNotebookAccess(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (updates: AccessForm[]) => updateNotebookAccess(id, updates),
    onSuccess: (acl) => patchNotebookDetails(queryClient, id, (notebook) => ({ ...notebook, acl })),
  });
}
