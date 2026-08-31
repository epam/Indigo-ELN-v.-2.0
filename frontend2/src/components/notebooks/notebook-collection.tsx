import {InfiniteLoader, type InfiniteLoaderLayout} from '@/components/common/infinite-loader';
import {NotebookCard} from '@/components/notebooks/notebook-card';
import {NotebookCardSkeleton} from '@/components/notebooks/notebook-card-skeleton';
import {NotebookRow} from '@/components/notebooks/notebook-row';
import {NotebookRowSkeleton} from '@/components/notebooks/notebook-row-skeleton';
import {NOTEBOOKS_PAGE_SIZE, useProjectNotebooks} from '@/lib/api/notebooks';

import type {CollectionFilters, CollectionView} from '@/lib/types/common.ts';
import type {Notebook} from '@/lib/types/notebooks.ts';

/** Same three-column cap as the projects grid — see ProjectCollection for the `max()` floor. */
const GRID_CLASS = 'grid gap-3 grid-cols-[repeat(auto-fill,minmax(max(280px,(100%_-_1.5rem)/3),1fr))]';

const LAYOUTS = {
  grid: { className: GRID_CLASS, Item: NotebookCard, ItemSkeleton: NotebookCardSkeleton },
  list: { className: 'flex flex-col gap-3', Item: NotebookRow, ItemSkeleton: NotebookRowSkeleton },
} satisfies Record<CollectionView, InfiniteLoaderLayout<Notebook>>;

export function NotebookCollection({
  projectId,
  filters,
  view,
}: {
  projectId: string;
  filters: CollectionFilters;
  view: CollectionView;
}) {
  return (
    <InfiniteLoader
      entityLabel="notebooks"
      view={view}
      layouts={LAYOUTS}
      query={useProjectNotebooks(projectId, filters)}
      firstLoadSkeletons={NOTEBOOKS_PAGE_SIZE}
    />
  );
}
