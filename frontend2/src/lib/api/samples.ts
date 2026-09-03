import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { InfiniteData } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';

import type { FindSamplesRequest, FindSamplesState, SampleDTO, SampleSearchResult } from '@/lib/types/samples.ts';

/**
 * The page size indigo-frontend's `SamplesSearchLoader` asks for. Large because a catalog page
 * is the unit the cursor advances in: a small one turns a scroll through PubChem's results into
 * a long chain of round trips, each of which has to be waited for before the next can be built.
 */
const SEARCH_PAGE_SIZE = 100;

const sampleKeys = {
  /**
   * The whole request is the key — TanStack Query hashes it structurally. That is what makes the
   * catalog radio work with no imperative refetch: `catalogs` is part of the request, so choosing
   * a different catalog moves the key, starts a new query, and leaves the previous catalog's
   * results in cache to come back instantly if the choice is undone.
   *
   * `state` is deliberately **not** in it. The cursor is a page param, not part of the question
   * being asked; keying on it would give every page its own cache entry.
   */
  search: (request: FindSamplesRequest) => ['sampleSearch', request] as const,
};

function searchSamples(
  request: FindSamplesRequest,
  state: FindSamplesState | null,
  signal?: AbortSignal,
): Promise<SampleSearchResult> {
  return apiFetch<SampleSearchResult>(`/api/eln/samples/search?pageSize=${SEARCH_PAGE_SIZE}`, {
    method: 'POST',
    // The cursor rides in the body, not the query string — `FindSamplesRequest.state`.
    body: JSON.stringify({ ...request, state: state ?? undefined }),
    signal,
  });
}

/**
 * A catalog search, paged by the cursor the server hands back.
 *
 * It has no disabled state: a search is only ever mounted for a structure that needs one, so
 * there is nothing to wait for and nothing to debounce — the structure is the whole query.
 * `signal` is consumed so switching catalogs aborts the request for the catalog no longer
 * selected, rather than letting it run to completion into a cache entry nothing will read.
 */
export function useSampleSearch(request: FindSamplesRequest) {
  return useInfiniteQuery({
    queryKey: sampleKeys.search(request),
    queryFn: ({ pageParam, signal }) => searchSamples(request, pageParam, signal),
    initialPageParam: null as FindSamplesState | null,
    // `next` is null on the last page of the last catalog, and that is the only end signal:
    // `totalItems` goes null as soon as a catalog that cannot count has contributed.
    getNextPageParam: (lastPage: SampleSearchResult) => lastPage.next ?? undefined,
  });
}

function markSample(id: string, marked: boolean): Promise<SampleDTO> {
  return apiFetch<SampleDTO>(`/api/eln/samples/${id}/${marked ? 'mark' : 'unmark'}`, { method: 'POST' });
}

/**
 * Adds a sample to, or removes it from, the signed-in user's My Materials list.
 *
 * The response is the updated `SampleDTO`, and it is written over the matching row in **every**
 * cached search rather than invalidating anything — the port of `InfiniteSearchLoader.replace`.
 * Two reasons it has to be a patch: a refetch would re-run a substructure search (and possibly a
 * PubChem call) to learn one boolean, and the My Materials catalog *filters* on the flag, so
 * refetching there would make the row the user just unmarked vanish from under the pointer.
 *
 * `setQueriesData` covers the prefix because the same sample can be a hit in more than one tab.
 */
export function useMarkSample() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, marked }: { id: string; marked: boolean }) => markSample(id, marked),
    onSuccess: (updated) => {
      queryClient.setQueriesData<InfiniteData<SampleSearchResult>>({ queryKey: ['sampleSearch'] }, (data) => {
        if (data == null) return data;
        return {
          ...data,
          pages: data.pages.map((page) => ({
            ...page,
            items: page.items.map((item) => (item.id != null && item.id === updated.id ? updated : item)),
          })),
        };
      });
    },
  });
}

/**
 * Registers a catalog hit that is not in the ELN yet, so it gains an id.
 *
 * The whole `SampleDTO` goes back as the body: `SampleSearchService.importSample` dispatches on
 * `source` to pick the provider, and the PubChem one reads `inchi` to load the structure and
 * `compoundKey` to record the external number. Sending a trimmed object would 500 on the far side.
 *
 * No cache write. The imported sample differs from the search hit by more than its id (it has a
 * batch number now), but the row it came from is about to be bound to an input row and the
 * mutation response repaints that; rewriting the search results as well would claim the catalog
 * had said something it did not.
 */
export function useImportSample() {
  return useMutation({
    mutationFn: (sample: SampleDTO) =>
      apiFetch<SampleDTO>('/api/eln/samples/importFromSearch', { method: 'POST', body: JSON.stringify(sample) }),
  });
}

/**
 * Where to fetch a catalog hit's structure. An ELN compound has a picture endpoint of its own;
 * a PubChem hit has only its InChI, which the backend renders on demand.
 *
 * Both are `@Cached(30, DAYS)` and neither takes a revision, so the path fully identifies the
 * bytes — which is what lets `ApiImage` cache on it forever.
 */
export function samplePicturePath(sample: SampleDTO): string | null {
  if (sample.compoundID != null) return `/api/eln/compounds/${sample.compoundID}/picture`;
  if (sample.inchi != null) return `/api/eln/samples/external/picture?inchi=${encodeURIComponent(sample.inchi)}`;
  return null;
}
