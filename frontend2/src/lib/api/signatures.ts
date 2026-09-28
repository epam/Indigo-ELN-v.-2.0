import { useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query';
import type { InfiniteData } from '@tanstack/react-query';

import { apiDownload, apiFetch } from '@/lib/api';
import { COLLECTION_PAGE_SIZE, getNextPageParam, useSettledSearch } from '@/lib/api/collections';
import { useDownload } from '@/lib/hooks/use-download';

import type { Page, UUID } from '@/lib/types/common.ts';
import type { SignatureDocument, SignatureFilters } from '@/lib/types/signatures.ts';

/**
 * One root, since the screen has one list and nothing else caches a document. `all` is what a
 * decision patches through — it matches every filter combination the user has visited.
 */
const signatureKeys = {
  all: () => ['signatureDocuments'] as const,
  list: (filters: SignatureFilters) => ['signatureDocuments', filters] as const,
};

/**
 * `collectionQueryParams`' counterpart for this endpoint. Same three fixed params, but the
 * boolean is `waitingMySignature` rather than `createdByMe`, and there is no `sortBy` — the
 * backend orders on `lastModifiedDate` whatever it is sent. Optional params are omitted when
 * unset, as the ELN lists do.
 */
function signatureQueryParams(filters: SignatureFilters, pageNo: number): URLSearchParams {
  const params = new URLSearchParams({
    sort: filters.sort,
    pageNo: String(pageNo),
    pageSize: String(COLLECTION_PAGE_SIZE),
  });
  if (filters.search) params.set('search', filters.search);
  if (filters.waitingMySignature) params.set('waitingMySignature', 'true');
  return params;
}

function fetchDocuments(
  filters: SignatureFilters,
  pageNo: number,
  signal?: AbortSignal,
): Promise<Page<SignatureDocument>> {
  return apiFetch<Page<SignatureDocument>>(`/api/signature/documents?${signatureQueryParams(filters, pageNo)}`, {
    signal,
  });
}

/**
 * The signatures list. Debounced the way every other list is — the key tracks the term as typed
 * while `useSettledSearch` gates `enabled`, so `isPending` covers both the wait and the request
 * and `InfiniteLoader` shows skeletons for the whole of it.
 */
export function useSignatureDocuments(filters: SignatureFilters) {
  const settled = useSettledSearch(filters.search);

  return useInfiniteQuery({
    queryKey: signatureKeys.list(filters),
    // Consuming `signal` lets an abandoned search abort instead of running to completion.
    queryFn: ({ pageParam, signal }) => fetchDocuments(filters, pageParam, signal),
    initialPageParam: 0,
    getNextPageParam,
    enabled: settled,
  });
}

/**
 * The signed (or still unsigned) PDF held by the signature service. Through `apiDownload` rather
 * than an `<a href download>` because the endpoint wants the Cognito bearer token; the response
 * names the file, and `filename` is the fallback when it does not.
 */
export function useDownloadDocument(document: SignatureDocument) {
  const { download, downloading } = useDownload();

  return {
    downloadReport: () =>
      void download(() => apiDownload(`/api/signature/documents/${document.id}/download`, document.filename)),
    downloading,
  };
}

export type SignatureDecision = 'sign' | 'reject';

/**
 * Approve or reject one document, as the current user's block on it.
 *
 * `signDocument` is declared `@Consumes(MULTIPART_FORM_DATA)` on a body-less POST, so both send an
 * empty `FormData` — that is what makes the browser set the boundary-carrying content type the
 * annotation asks for.
 *
 * The response is the whole updated document, and it is **patched into the cached pages** rather
 * than invalidating them. Acting moves `lastModifiedDate`, which is the sort key, so a refetch
 * would send the row the user just acted on to the top of the list under the default `LATEST`
 * sort — the one place the reader is not looking. A failure needs no handling here: `apiFetch`
 * has already toasted it, the cache is untouched, and the buttons leave their loading state.
 */
export function useSignatureDecision(documentId: UUID) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (decision: SignatureDecision) =>
      apiFetch<SignatureDocument>(`/api/signature/documents/${documentId}/${decision}`, {
        method: 'POST',
        formData: new FormData(),
      }),
    onSuccess: (updated) => {
      queryClient.setQueriesData<InfiniteData<Page<SignatureDocument>>>(
        { queryKey: signatureKeys.all() },
        (previous) =>
          previous && {
            ...previous,
            pages: previous.pages.map((page) => ({
              ...page,
              items: page.items.map((item) => (item.id === updated.id ? updated : item)),
            })),
          },
      );
    },
  });
}
