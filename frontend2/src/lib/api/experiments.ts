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
  });
}
