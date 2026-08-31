import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { TemplateDetails } from '@/lib/types/templates.ts';

const templateKeys = {
  detail: (id: string) => ['templateDetails', id] as const,
};

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
