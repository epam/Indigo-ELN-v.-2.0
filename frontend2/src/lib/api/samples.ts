import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { InfiniteData } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { getNextPageParam } from '@/lib/api/collections';
import { sampleRowKey } from '@/lib/search';

import type { Page } from '@/lib/types/common.ts';
import type { FindSamplesRequest, SampleDTO } from '@/lib/types/samples.ts';

/**
 * Large because PubChem cannot page: it answers page 0 and nothing after, so its one page has to
 * hold everything worth showing. The other catalogs page normally at the same size.
 */
const SEARCH_PAGE_SIZE = 100;

const sampleKeys = {
  /**
   * The whole request is the key — TanStack Query hashes it structurally. That is what makes the
   * catalog radio work with no imperative refetch: `catalog` is part of the request, so choosing
   * a different catalog moves the key, starts a new query, and leaves the previous catalog's
   * results in cache to come back instantly if the choice is undone.
   */
  search: (request: FindSamplesRequest) => ['sampleSearch', request] as const,
};

function searchSamples(request: FindSamplesRequest, pageNo: number, signal?: AbortSignal): Promise<Page<SampleDTO>> {
  return apiFetch<Page<SampleDTO>>(`/api/eln/samples/search?pageNo=${pageNo}&pageSize=${SEARCH_PAGE_SIZE}`, {
    method: 'POST',
    json: request,
    signal,
  });
}

/**
 * A catalog search, paged like every other list.
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
    initialPageParam: 0,
    getNextPageParam,
  });
}

/**
 * Adds a sample to, or removes it from, the signed-in user's My Materials list. The whole
 * `SampleDTO` is the body: the backend keys the mark on `source` + `sampleKey` and imports the
 * compound from `catalog`.
 *
 * The response is the updated `SampleDTO`, and its `marked` is written over the matching row in
 * **every** cached search rather than invalidating anything — the port of
 * `InfiniteSearchLoader.replace`. Two reasons it has to be a patch: a refetch would re-run a
 * substructure search (and possibly a PubChem call) to learn one boolean, and the My Materials
 * catalog *filters* on the flag, so refetching there would make the row the user just unmarked
 * vanish from under the pointer.
 *
 * Only the flag is copied, not the row: the same sample shows in its own catalog's tab and in My
 * Materials, and each tab keeps the record its own catalog answered with.
 */
export function useMarkSample() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ sample, marked }: { sample: SampleDTO; marked: boolean }) =>
      apiFetch<SampleDTO>(`/api/eln/samples/${marked ? 'mark' : 'unmark'}`, { method: 'POST', json: sample }),
    onSuccess: (updated) => {
      const key = sampleRowKey(updated);
      queryClient.setQueriesData<InfiniteData<Page<SampleDTO>>>({ queryKey: ['sampleSearch'] }, (data) => {
        if (data == null) return data;
        return {
          ...data,
          pages: data.pages.map((page) => ({
            ...page,
            items: page.items.map((item) => (sampleRowKey(item) === key ? { ...item, marked: updated.marked } : item)),
          })),
        };
      });
    },
  });
}

/** How to draw a catalog hit's structure: fetch it from an API path, or render its InChI locally. */
export type SamplePicture = { path: string } | { inchi: string };

/**
 * Where a catalog hit's structure comes from, in order — or null when there is nowhere:
 *
 * - A My Materials hit's `compoundID` names an ELN compound, which has a picture endpoint of its own.
 * - A PubChem hit carries its InChI, which Ketcher renders in the browser.
 * - Any other hit with a `compoundID` has its id in `source`, which its catalog serves the
 *   picture by — an SRS hit's `compoundID` is an SRS id, meaningless to the ELN directly.
 *
 * Both paths are `@Cached(30, DAYS)` and neither takes a revision, so the path fully identifies
 * the bytes — which is what lets `ApiImage` cache on it forever.
 */
export function samplePicture(sample: SampleDTO): SamplePicture | null {
  if (sample.catalog === 'MY_MATERIALS' && sample.compoundID != null) {
    return { path: `/api/eln/compounds/${sample.compoundID}/picture` };
  }
  if (sample.inchi != null) return { inchi: sample.inchi };
  if (sample.compoundID != null) {
    return { path: `/api/eln/compounds/by-catalog/${sample.catalog}/${sample.source}/${sample.compoundID}/picture` };
  }
  return null;
}
