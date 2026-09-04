import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { Page } from '@/lib/types/common.ts';
import type { Template, TemplateDetails } from '@/lib/types/templates.ts';

const templateKeys = {
  list: () => ['templates'] as const,
  detail: (id: string) => ['templateDetails', id] as const,
};

/**
 * One page big enough to be the whole list. `/templates` is `@BeanParam Paging`, whose
 * `DEFAULT_PAGE_SIZE` is 10 — so a request that does not say silently offers only the ten most
 * recently modified templates, which is a live bug in indigo-frontend's Add Experiment dialog.
 */
const TEMPLATE_PAGE_SIZE = 100;

function fetchTemplates(signal?: AbortSignal): Promise<Page<Template>> {
  return apiFetch<Page<Template>>(`/api/eln/templates?pageSize=${TEMPLATE_PAGE_SIZE}`, { signal });
}

/**
 * Every template, for the Add Experiment picker. Enabled by the caller only while that dialog is
 * open, so a notebook page nobody creates from never asks.
 *
 * Two deliberate differences from `useNextNotebookNumber`, the other query a dialog's initializing
 * phase waits on:
 *
 * - **No `staleTime: 0`.** A notebook number is consumed by the create that follows it, so a
 *   cached one is wrong by the time it is reused; a template list is not, and the client's 30 s
 *   default is right.
 * - Its caller gates `initializing` on **`isPending` alone**, not `isPending || isFetching`. A
 *   background refetch of a list already on screen must not re-freeze a dialog someone is filling
 *   in — the opposite of the notebook case, where showing the stale value was the whole problem.
 *
 * The list arrives in the server's order, which is by `modifiedAt`: `SortOrder` is `EARLIEST` /
 * `LATEST` and offers nothing alphabetical, so there is no better order to ask for.
 */
export function useTemplates(enabled: boolean) {
  return useQuery({
    queryKey: templateKeys.list(),
    queryFn: ({ signal }) => fetchTemplates(signal),
    enabled,
  });
}

function fetchTemplate(id: string, signal?: AbortSignal): Promise<TemplateDetails> {
  return apiFetch<TemplateDetails>(`/api/eln/templates/${id}`, { signal });
}

/**
 * The template an experiment is laid out by. The id only exists once the experiment itself has
 * loaded, so this is the second of two chained queries and stays disabled until then — passing
 * `undefined` is the normal first render, not an error.
 */
export function useTemplate(id: string | undefined) {
  return useQuery({
    queryKey: templateKeys.detail(id ?? ''),
    queryFn: ({ signal }) => fetchTemplate(id!, signal), // enabled below guarantees it is set
    enabled: id !== undefined,
  });
}
