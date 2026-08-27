import { InfiniteLoader, type InfiniteLoaderLayout } from '@/components/common/infinite-loader';
import { ProjectCard } from '@/components/projects/project-card';
import { ProjectCardSkeleton } from '@/components/projects/project-card-skeleton';
import { ProjectRow } from '@/components/projects/project-row';
import { ProjectRowSkeleton } from '@/components/projects/project-row-skeleton';
import { PROJECTS_PAGE_SIZE, useProjects } from '@/lib/api/projects';

import type { CollectionView } from '@/lib/types/common.ts';
import type { Project, ProjectFilters } from '@/lib/types/projects';

/**
 * Three columns at most, dropping to two then one when the grid is too narrow for
 * 280px cards. The `max()` floor of a third of the row (minus the two 12px gaps)
 * is what caps it at three — without it `auto-fill` would keep adding columns on a
 * wide screen. Measured against the grid itself, so collapsing the sidebar reflows
 * it just like resizing the window does.
 */
const GRID_CLASS = 'grid gap-3 grid-cols-[repeat(auto-fill,minmax(max(280px,(100%_-_1.5rem)/3),1fr))]';

/** The two layouts differ only in their container and which item component fills it. */
const LAYOUTS = {
  grid: { className: GRID_CLASS, Item: ProjectCard, ItemSkeleton: ProjectCardSkeleton },
  list: { className: 'flex flex-col gap-3', Item: ProjectRow, ItemSkeleton: ProjectRowSkeleton },
} satisfies Record<CollectionView, InfiniteLoaderLayout<Project>>;

export function ProjectCollection({ filters, view }: { filters: ProjectFilters; view: CollectionView }) {
  return (
    <InfiniteLoader
      entityLabel="projects"
      view={view}
      layouts={LAYOUTS}
      query={useProjects(filters)}
      firstLoadSkeletons={PROJECTS_PAGE_SIZE}
    />
  );
}
