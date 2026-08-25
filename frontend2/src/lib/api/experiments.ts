import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { Experiment } from '@/lib/types/experiments.ts';

export const experimentKeys = {
  marked: () => ['experiments', 'marked'] as const,
  picture: (id: string, revision: number | null) => ['experiments', id, 'picture', revision] as const,
};

/** Unpaged: ExperimentAPI.getMarkedExperiments returns the full list. */
export function fetchMarkedExperiments(): Promise<Experiment[]> {
  return apiFetch<Experiment[]>('experiments/marked');
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
 * The reaction scheme, as an SVG string.
 *
 * `apiFetch` already hands this back as text: `parseBody` reads the body, fails to parse it
 * as JSON, and returns the string — so there is no blob or object URL to own here, and the
 * SVG can go straight into an `<img>` as a data URL.
 *
 * `revision` is a cache-buster only; ExperimentResource ignores it, and the endpoint
 * declares a 30-day cache.
 */
export function fetchExperimentPicture(id: string, revision: number | null, signal?: AbortSignal): Promise<string> {
  const query = revision === null ? '' : `?revision=${revision}`;
  return apiFetch<string>(`experiments/${id}/picture${query}`, { signal });
}

export function useExperimentPicture(id: string, revision: number | null) {
  return useQuery({
    queryKey: experimentKeys.picture(id, revision),
    queryFn: ({ signal }) => fetchExperimentPicture(id, revision, signal),
    // The revision is part of the key, so a cached picture can never go stale.
    staleTime: Infinity,
  });
}
