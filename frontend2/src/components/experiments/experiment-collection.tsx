import { InfiniteLoader, type InfiniteLoaderLayout } from '@/components/common/infinite-loader';
import { ExperimentCard } from '@/components/experiments/experiment-card';
import { ExperimentCardSkeleton } from '@/components/experiments/experiment-card-skeleton';
import { ExperimentRow } from '@/components/experiments/experiment-row';
import { ExperimentRowSkeleton } from '@/components/experiments/experiment-row-skeleton';
import { EXPERIMENTS_PAGE_SIZE, useNotebookExperiments } from '@/lib/api/experiments';

import type { CollectionView } from '@/lib/types/common.ts';
import type { Experiment, ExperimentFilters } from '@/lib/types/experiments.ts';

/** Same three-column cap as the projects grid — see ProjectCollection for the `max()` floor. */
const GRID_CLASS = 'grid gap-3 grid-cols-[repeat(auto-fill,minmax(max(280px,(100%_-_1.5rem)/3),1fr))]';

const LAYOUTS = {
  grid: { className: GRID_CLASS, Item: ExperimentCard, ItemSkeleton: ExperimentCardSkeleton },
  list: { className: 'flex flex-col gap-3', Item: ExperimentRow, ItemSkeleton: ExperimentRowSkeleton },
} satisfies Record<CollectionView, InfiniteLoaderLayout<Experiment>>;

export function ExperimentCollection({
  notebookId,
  filters,
  view,
}: {
  notebookId: string;
  filters: ExperimentFilters;
  view: CollectionView;
}) {
  return (
    <InfiniteLoader
      entityLabel="experiments"
      view={view}
      layouts={LAYOUTS}
      query={useNotebookExperiments(notebookId, filters)}
      firstLoadSkeletons={EXPERIMENTS_PAGE_SIZE}
    />
  );
}
