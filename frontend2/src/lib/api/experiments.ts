import { useInfiniteQuery, useIsMutating, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { QueryClient } from '@tanstack/react-query';

import { apiDownload, apiFetch } from '@/lib/api';
import { collectionQueryString, getNextPageParam, SEARCH_DEBOUNCE_MS } from '@/lib/api/collections';
import { useDownload } from '@/lib/hooks/use-download';
import { useSettled } from '@/lib/hooks/use-settled';
import { JSON_PATCHER } from '@/lib/json-patcher';
import { notifyInfo } from '@/lib/toast';
import type { AccessForm, ACLEntry, Attachment, Page } from '@/lib/types/common.ts';
import type {
  Experiment,
  ExperimentDetails,
  ExperimentEditRequest,
  ExperimentFilters,
  ExperimentRef,
} from '@/lib/types/experiments.ts';
import type { ModelMutation, MutationResponse } from '@/lib/types/mutations.ts';

export const EXPERIMENTS_PAGE_SIZE = 10;

/**
 * Every query key this module issues, written out literally rather than composed from a shared
 * prefix — the same shape, and for the same reasons, as `projectKeys` in `projects.ts`.
 *
 * Details sit under their own `experimentDetails` root, not under `experiments`. Starring or
 * editing an experiment invalidates every list, and a detail nested beneath that prefix would be
 * caught by the same call and refetched immediately after the mutation response had been written
 * into it.
 *
 * Filters belong in the list key; the page number comes from pageParam.
 *
 * Exported although no component reads it: `src/lib/query-client.ts` hashes `marked()` to decide
 * what gets persisted to localStorage. Not a candidate for going private.
 */
export const experimentKeys = {
  all: () => ['experiments'] as const,
  marked: () => ['experiments', 'marked'] as const,
  notebookList: (notebookId: string, filters: ExperimentFilters) =>
    ['experiments', 'notebook', notebookId, filters] as const,
  detail: (id: string) => ['experimentDetails', id] as const,
  suggestions: (search: string) => ['experimentSuggestions', search] as const,
};

/**
 * What every write to one experiment shares: a key to count them by, and a `scope` that makes
 * TanStack Query run them **one at a time**.
 *
 * The serialisation is not cosmetic. Each of these endpoints goes through
 * `ExperimentModelService.applyMutation` on the backend, which bumps the experiment's `revision`
 * — the token the model-mutation endpoint uses for optimistic concurrency. Two writes in flight
 * race on it. Since fields save on blur, a quick user starts the second before the first lands,
 * so this is the normal case rather than an edge one.
 *
 * `scope` does the whole job: `MutationCache.canRun` lets only the first pending mutation of a
 * scope proceed and pauses the rest, and `runNext` fires from a `finally`, so a failed write
 * hands off instead of wedging the queue. Two consequences worth knowing:
 *
 * - A **queued** mutation already reports `isPending: true` (with `isPaused: true`), so a control
 *   gated on `isPending` stays frozen for the wait as well as the request. Nothing extra to track.
 * - `onMutate` runs when `mutate()` is called, **not** when the request finally starts. Nothing
 *   uses it here yet, but an optimistic update added later would land while still queued.
 */
function experimentWrite(id: string) {
  return { mutationKey: experimentWriteKey(id), scope: { id: `experiment-${id}` } };
}

function experimentWriteKey(id: string) {
  return ['experiment', id, 'write'] as const;
}

/**
 * Whether any write to this experiment is queued or running — the page-level roll-up behind the
 * spinner over the undo/redo buttons. `useIsMutating` filters on `status: 'pending'`, which covers
 * both states.
 */
export function useExperimentSaving(id: string): boolean {
  return useIsMutating({ mutationKey: experimentWriteKey(id) }) > 0;
}

function fetchExperiment(id: string, signal?: AbortSignal): Promise<ExperimentDetails> {
  return apiFetch<ExperimentDetails>(`/api/eln/experiments/${id}`, { signal });
}

export function useExperiment(id: string) {
  return useQuery({
    queryKey: experimentKeys.detail(id),
    queryFn: ({ signal }) => fetchExperiment(id, signal),
  });
}

/** Patches one field of the cached detail, leaving the rest of the experiment untouched. */
function patchExperimentDetails(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  patch: (experiment: ExperimentDetails) => ExperimentDetails,
) {
  queryClient.setQueryData<ExperimentDetails>(experimentKeys.detail(id), (experiment) =>
    experiment ? patch(experiment) : experiment,
  );
}

function editExperiment(id: string, request: ExperimentEditRequest): Promise<ExperimentDetails> {
  return apiFetch<ExperimentDetails>(`/api/eln/experiments/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

/**
 * The experiment has no edit dialog: fields save as they are left, so this fires far more often
 * than the project and notebook equivalents and callers are expected to skip an unchanged value.
 *
 * The response is the whole experiment, so the detail needs no refetch — but `modifiedAt` shows
 * on every card of the parent notebook's list, and those do.
 */
export function useEditExperiment(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...experimentWrite(id),
    mutationFn: (request: ExperimentEditRequest) => editExperiment(id, request),
    onSuccess: (experiment) => {
      queryClient.setQueryData(experimentKeys.detail(id), experiment);
      void queryClient.invalidateQueries({ queryKey: experimentKeys.all() });
    },
  });
}

/**
 * The model-mutation endpoint: every edit to the reaction tree goes through one `Mutation`
 * and comes back as a **JSON diff** rather than a new experiment, which is what keeps a
 * response to a one-cell edit from carrying the whole stoichiometry table back.
 *
 * The backend's `mutateModel` accepts `revision` and never reads it: there is no optimistic
 * concurrency check today, and the patch itself never carries a new revision (`revision` is
 * in `JSONPatcher.EXPERIMENT_IGNORED_PATHS`), so a patched copy keeps the revision it had and
 * no amount of care on this side would keep it fresh. It is sent to match the contract, not
 * to guard anything — do not build conflict handling on it.
 */
function mutateExperimentModel(id: string, revision: number, mutation: ModelMutation): Promise<MutationResponse> {
  return apiFetch<MutationResponse>(`/api/eln/experiments/${id}/mutate?revision=${revision}`, {
    method: 'POST',
    body: JSON.stringify(mutation),
  });
}

/**
 * Takes the experiment rather than its id because it needs the revision, and the caller
 * always has one in hand — a panel is rendering it. Nothing here refetches.
 *
 * `onPatched` receives `JSON_PATCHER.apply`'s second return value: a `newNode -> oldNode` map
 * covering every object the diff rebuilt. That is how the stoichiometry table tells a cell the
 * backend recalculated from one it left alone — a patch says which *nodes* changed, never
 * which cells, so the comparison has to happen against the node that was replaced.
 *
 * The map's **keys** are the objects written into the query cache, so a later render reading
 * `sample.weight` back out gets an identity hit. Its **values** are clones of the previous
 * state (`apply` starts from a `structuredClone`), so they are good for comparing fields and
 * useless for comparing identity.
 */
export function useMutateExperimentModel(
  experiment: ExperimentDetails,
  onPatched?: (updatedNodes: ReadonlyMap<unknown, unknown>) => void,
) {
  const queryClient = useQueryClient();
  const id = experiment.id;

  return useMutation({
    ...experimentWrite(id),
    mutationFn: (mutation: ModelMutation) => {
      // Prefer the cached copy, which may be newer than the one this component rendered with:
      // `useEditExperiment` replaces the whole detail, revision included, and this mutation
      // may have been sitting in `experimentWrite`'s queue while that landed. The prop is the
      // fallback, and the only copy there is in a story or test that never filled the cache.
      const current = queryClient.getQueryData<ExperimentDetails>(experimentKeys.detail(id)) ?? experiment;
      return mutateExperimentModel(id, current.revision, mutation);
    },
    onSuccess: (response) => applyMutationResponse(queryClient, id, response, onPatched),
  });
}

/**
 * What every endpoint answering with a `MutationResponse` has to do with it: apply the diff to the
 * cached detail, report the nodes it rebuilt, toast whatever the backend said, and refresh the
 * lists. Shared by `/mutate` and by the SDF import, which is a `MutationResponse` reached through
 * its own multipart endpoint.
 */
function applyMutationResponse(
  queryClient: QueryClient,
  id: string,
  response: MutationResponse,
  onPatched?: (updatedNodes: ReadonlyMap<unknown, unknown>) => void,
): void {
  // The diff is computed between two ExperimentSnapshots rather than two
  // ExperimentDetailsDTOs. They overlap on names, which is why applying it to the detail
  // works — but it never touches currentPermissions, marked, the ancestor ids and names,
  // or the BaseDTO audit fields, so those survive untouched by construction.
  let updatedNodes: ReadonlyMap<unknown, unknown> = new Map();
  patchExperimentDetails(queryClient, id, (experiment) => {
    const [patched, nodes] = JSON_PATCHER.apply(experiment, response.patch);
    updatedNodes = nodes;
    return patched as ExperimentDetails;
  });
  // After the cache write, so a subscriber re-rendering on the new data already has it.
  // Skipped entirely when the detail was not cached — `patchExperimentDetails` no-ops
  // there, and reporting an empty map would read as "nothing changed".
  if (updatedNodes.size > 0) onPatched?.(updatedNodes);
  // TODO(analyze-rxn): response.unresolvedInputs names reactants the backend could not
  // match to a compound. Resolving them needs indigo-frontend's AnalyzeRxn slide-in panel
  // and the ResolveInputs mutation, neither of which is ported yet.
  for (const message of response.messages ?? []) notifyInfo(message);
  // The write bumps modifiedAt, which every list card shows.
  void queryClient.invalidateQueries({ queryKey: experimentKeys.all() });
}

function importSdf(id: string, reactionAnchor: string, file: File): Promise<MutationResponse> {
  const body = new FormData();
  // `UploadForm.file` — the part name is `@FormParam("file")` on the Java record.
  body.append('file', file, file.name);
  return apiFetch<MutationResponse>(`/api/eln/experiments/${id}/datamodel/reactions/${reactionAnchor}/importSDF`, {
    method: 'POST',
    body,
  });
}

/**
 * Creates a product row per compound in an uploaded SDF.
 *
 * `ImportSDF` is a mutation type like any other on the backend, but `isMutateMethodAllowed()` is
 * false for it — the file has to arrive as multipart — so it has its own endpoint and is
 * deliberately absent from `ModelMutation`. What comes back is the same `MutationResponse` as
 * `/mutate`, and it is applied the same way.
 *
 * Joins `experimentWrite`'s scope so an import queues behind the on-blur cell saves instead of
 * racing them.
 */
export function useImportSdf(
  experiment: ExperimentDetails,
  reactionAnchor: string,
  onPatched?: (updatedNodes: ReadonlyMap<unknown, unknown>) => void,
) {
  const queryClient = useQueryClient();
  const id = experiment.id;

  return useMutation({
    ...experimentWrite(id),
    mutationFn: (file: File) => importSdf(id, reactionAnchor, file),
    onSuccess: (response) => applyMutationResponse(queryClient, id, response, onPatched),
  });
}

/**
 * Downloads the experiment's reactions as an SDF.
 *
 * A plain GET, so it needs no revision and no cache handling — but it still has to go through
 * `apiDownload` rather than an `<a href download>`, since the endpoint wants the Cognito bearer
 * token. `useDownload` swallows the rejection `apiFetch` has already toasted.
 */
export function useExportSdf(id: string) {
  const { download, downloading } = useDownload();

  return {
    exportSdf: () => void download(() => apiDownload(`/api/eln/experiments/${id}/exportSdf`, 'export.sdf')),
    exporting: downloading,
  };
}

/** Returns the experiment's full attachment list, not just the new entries. */
function uploadExperimentAttachment(id: string, file: File): Promise<Attachment[]> {
  const body = new FormData();
  body.append('file', file, file.name);
  return apiFetch<Attachment[]>(`/api/eln/experiments/${id}/attachments`, { method: 'POST', body });
}

function deleteExperimentAttachment(id: string, attachmentId: string): Promise<void> {
  return apiFetch<void>(`/api/eln/experiments/${id}/attachments/${attachmentId}`, { method: 'DELETE' });
}

/** See `useNotebookAttachments` — the endpoints are identical bar the prefix. */
function useUploadExperimentAttachments(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...experimentWrite(id),
    // One at a time within this mutation too: the endpoint takes a single `file` part and each
    // response carries the full list, so parallel uploads would race and the last one home would
    // drop the others. The scope above serialises it against *other* writes; this loop serialises
    // the files of one upload against each other.
    mutationFn: async (files: File[]) => {
      let attachments: Attachment[] = [];
      for (const file of files) {
        attachments = await uploadExperimentAttachment(id, file);
      }
      return attachments;
    },
    onSuccess: (attachments) =>
      patchExperimentDetails(queryClient, id, (experiment) => ({ ...experiment, attachments })),
  });
}

function useDeleteExperimentAttachment(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...experimentWrite(id),
    mutationFn: (attachmentId: string) => deleteExperimentAttachment(id, attachmentId),
    onSuccess: (_result, attachmentId) =>
      patchExperimentDetails(queryClient, id, (experiment) => ({
        ...experiment,
        attachments: experiment.attachments.filter((attachment) => attachment.id !== attachmentId),
      })),
  });
}

function updateExperimentAccess(id: string, updates: AccessForm[]): Promise<ACLEntry[]> {
  return apiFetch<ACLEntry[]>(`/api/eln/experiments/${id}/access`, {
    method: 'POST',
    body: JSON.stringify(updates),
  });
}

/**
 * The experiment half of `useUpdateNotebookAccess`: only what changed needs sending, and the
 * response is the recomputed ACL for the whole experiment — inherited entries included — so it
 * replaces `acl` wholesale rather than being merged in.
 *
 * The Team surfaces read `ExperimentDetails.acl` and nothing else — `ExperimentDTO.acl` is
 * `shortACL`, capped at three, and an experiment opened by direct link has no list loaded at all.
 * So the detail is the one copy that has to be written, and the response is written into it
 * verbatim.
 *
 * Unlike the project and notebook versions this carries `experimentWrite`'s key and scope.
 * `ExperimentService.updateExperimentAccess` runs the change through `applyMutation`, so it bumps
 * `revision` exactly like a field edit does and must queue behind the on-blur writes rather than
 * race them.
 */
export function useUpdateExperimentAccess(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...experimentWrite(id),
    mutationFn: (updates: AccessForm[]) => updateExperimentAccess(id, updates),
    onSuccess: (acl) => patchExperimentDetails(queryClient, id, (experiment) => ({ ...experiment, acl })),
  });
}

/** The three calls `AttachmentList` needs, bound to one experiment. */
export function useExperimentAttachments(id: string) {
  const upload = useUploadExperimentAttachments(id);
  const remove = useDeleteExperimentAttachment(id);

  return {
    upload,
    remove,
    download: (attachment: Attachment) =>
      apiDownload(`/api/eln/experiments/${id}/attachments/${attachment.id}`, attachment.name),
  };
}

function suggestExperiments(search: string, signal?: AbortSignal): Promise<ExperimentRef[]> {
  return apiFetch<ExperimentRef[]>(`/api/eln/experiments/suggest?search=${encodeURIComponent(search)}`, { signal });
}

const SUGGEST_DEBOUNCE_MS = 300;

/**
 * Experiments to reference from another one. Same shape as `useUserSuggestions`: the key tracks
 * the term as typed and the debounce gates `enabled`, so `isPending` is one honest "we don't know
 * yet" spanning both the wait and the request — which is what the combobox's `loading` prop wants.
 *
 * Held off until something is typed. `ExperimentRepository.suggest` answers a missing term with
 * the first ten experiments by name **across the whole system**, which is arbitrary rather than
 * helpful — it is not scoped to this notebook or project.
 */
export function useExperimentSuggestions(search: string) {
  const settled = useSettled(search, SUGGEST_DEBOUNCE_MS);

  return useQuery({
    queryKey: experimentKeys.suggestions(search),
    // Consuming `signal` lets an abandoned lookup abort when the key moves on.
    queryFn: ({ signal }) => suggestExperiments(search, signal),
    enabled: settled && search.length > 0,
  });
}

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
    // Deliberately **not** in `experimentWrite`'s queue. `ExperimentService.markExperiment` does
    // not go through `applyMutation` — it writes a per-user flag and needs only VIEW_EXPERIMENTS
    // — so it cannot conflict with anything. Queueing it would only make the star lag behind an
    // unrelated save.
    mutationFn: ({ id, marked }: { id: string; marked: boolean }) => setMarked(id, marked),
    // One prefix: `experimentKeys.marked()` and `notebookList()` both start with 'experiments'.
    // The detail is on its own root and so is left alone — a surface reading `marked` off it
    // (the experiment header) has to patch it here when the star there is wired up.
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: experimentKeys.all() }),
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
