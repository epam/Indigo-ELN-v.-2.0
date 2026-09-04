import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiDownload, apiFetch } from '@/lib/api';
import { collectionQueryString, getNextPageParam, SEARCH_DEBOUNCE_MS } from '@/lib/api/collections';
import { useSettled } from '@/lib/hooks/use-settled';
import type { AccessForm, ACLEntry, Attachment, CollectionFilters, Page } from '@/lib/types/common.ts';
import type { Project, ProjectDetails, ProjectEditRequest, ProjectRequest, TotalCounts } from '@/lib/types/projects.ts';

export const PROJECTS_PAGE_SIZE = 10;

function fetchProjects(filters: CollectionFilters, pageNo: number, signal?: AbortSignal): Promise<Page<Project>> {
  return apiFetch<Page<Project>>(`/api/eln/projects?${collectionQueryString(filters, pageNo, PROJECTS_PAGE_SIZE)}`, {
    signal,
  });
}

function fetchTotalCounts(): Promise<TotalCounts> {
  return apiFetch<TotalCounts>('/api/eln/total-counts');
}

function createProject(request: ProjectRequest): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>('/api/eln/projects', { method: 'POST', body: JSON.stringify(request) });
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
export function checkProjectNameExists(name: string): Promise<boolean> {
  return apiFetch<{ exists: boolean }>(`/api/eln/projects/existence?name=${encodeURIComponent(name)}`).then(
    (result) => result.exists,
  );
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
  detail: (id: string) => ['projectDetails', id] as const,
  totalCounts: () => ['totalCounts'] as const,
  keywordSuggestions: (search: string) => ['projectKeywords', search] as const,
};

/**
 * Debounced by gating `enabled` while the key tracks the search term as typed, so
 * `isPending` spans both the wait and the request — InfiniteLoader then shows its skeletons
 * for the whole time rather than leaving the previous term's results up unannounced.
 * Only the search term is debounced; sort and createdByMe are discrete toggles that
 * should take effect at once, and leave `settled` alone.
 */
export function useProjects(filters: CollectionFilters) {
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

const SUGGEST_DEBOUNCE_MS = 300;

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

function fetchProject(id: string, signal?: AbortSignal): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>(`/api/eln/projects/${id}`, { signal });
}

export function useProject(id: string) {
  return useQuery({
    queryKey: projectKeys.detail(id),
    queryFn: ({ signal }) => fetchProject(id, signal),
  });
}

function editProject(id: string, request: ProjectEditRequest): Promise<ProjectDetails> {
  return apiFetch<ProjectDetails>(`/api/eln/projects/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

export function useEditProject(id: string) {
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

/** Returns the project's full attachment list, not just the new entries. */
function uploadProjectAttachment(id: string, file: File): Promise<Attachment[]> {
  const body = new FormData();
  body.append('file', file, file.name);
  return apiFetch<Attachment[]>(`/api/eln/projects/${id}/attachments`, { method: 'POST', body });
}

function deleteProjectAttachment(id: string, attachmentId: string): Promise<void> {
  return apiFetch<void>(`/api/eln/projects/${id}/attachments/${attachmentId}`, { method: 'DELETE' });
}

/**
 * Saves the attachment to disk. The endpoint sets `Content-Disposition` from the same name the
 * DTO carries, so the fallback matters only if that header is ever stripped in transit.
 */
function downloadProjectAttachment(id: string, attachmentId: string, fallbackFilename: string): Promise<void> {
  return apiDownload(`/api/eln/projects/${id}/attachments/${attachmentId}`, fallbackFilename);
}

/** Patches one field of the cached detail, leaving the rest of the project untouched. */
function patchProjectDetails(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  patch: (project: ProjectDetails) => ProjectDetails,
) {
  queryClient.setQueryData<ProjectDetails>(projectKeys.detail(id), (project) => (project ? patch(project) : project));
}

/**
 * Files are uploaded one at a time: the endpoint takes a single `file` part, and each response
 * carries the full list, so a parallel upload would race and the last response home would drop
 * the others. `attachments` is read from the final response rather than accumulated.
 */
function useUploadAttachments(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (files: File[]) => {
      let attachments: Attachment[] = [];
      for (const file of files) {
        attachments = await uploadProjectAttachment(id, file);
      }
      return attachments;
    },
    onSuccess: (attachments) => patchProjectDetails(queryClient, id, (project) => ({ ...project, attachments })),
  });
}

function useDeleteAttachment(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (attachmentId: string) => deleteProjectAttachment(id, attachmentId),
    onSuccess: (_result, attachmentId) =>
      patchProjectDetails(queryClient, id, (project) => ({
        ...project,
        attachments: project.attachments.filter((attachment) => attachment.id !== attachmentId),
      })),
  });
}

/**
 * The three calls `AttachmentList` needs, bound to one project. Returned as a plain object so
 * the component stays ignorant of which entity it is attached to; the shape is checked
 * structurally against `AttachmentActions` where it is passed in.
 */
export function useProjectAttachments(id: string) {
  const upload = useUploadAttachments(id);
  const remove = useDeleteAttachment(id);

  return {
    upload,
    remove,
    download: (attachment: Attachment) => downloadProjectAttachment(id, attachment.id, attachment.name),
  };
}

function updateProjectAccess(id: string, updates: AccessForm[]): Promise<ACLEntry[]> {
  return apiFetch<ACLEntry[]>(`/api/eln/projects/${id}/access`, {
    method: 'POST',
    body: JSON.stringify(updates),
  });
}

/**
 * `ACLService.updateProjectACL` upserts entry by entry, so only what changed needs sending.
 * The response is the recomputed ACL for the whole project — inherited entries included — so
 * it replaces `acl` wholesale rather than being merged in.
 */
export function useUpdateProjectAccess(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (updates: AccessForm[]) => updateProjectAccess(id, updates),
    onSuccess: (acl) => patchProjectDetails(queryClient, id, (project) => ({ ...project, acl })),
  });
}
