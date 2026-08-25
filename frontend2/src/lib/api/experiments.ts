import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { Experiment } from '@/lib/types/experiments.ts';

export const experimentKeys = {
  marked: () => ['experiments', 'marked'] as const,
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
 * Where to fetch an experiment's reaction scheme. `revision` is a cache-buster only —
 * ExperimentResource ignores it, and the endpoint declares a 30-day cache — so putting it
 * in the path is also what keeps the client-side cache entry correct.
 */
export function experimentPicturePath(id: string, revision: number | null): string {
  const query = revision === null ? '' : `?revision=${revision}`;
  return `experiments/${id}/picture${query}`;
}
