import { useEffect, useRef } from 'react';

import type { ProjectView } from '@/components/projects/action-bar';
import { ProjectCard } from '@/components/projects/project-card';
import { ProjectCardSkeleton } from '@/components/projects/project-card-skeleton';
import { ProjectRow } from '@/components/projects/project-row';
import { ProjectRowSkeleton } from '@/components/projects/project-row-skeleton';
import { PROJECTS_PAGE_SIZE, useProjects } from '@/lib/api/projects';
import type { ProjectFilters } from '@/lib/types/projects';

/** A full page on first load, so a full first page lands without resizing the document. */
const FIRST_LOAD_SKELETONS = PROJECTS_PAGE_SIZE;
const NEXT_PAGE_SKELETONS = 3;

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
} as const;

export function ProjectCollection({ filters, view }: { filters: ProjectFilters; view: ProjectView }) {
  const { className, Item, ItemSkeleton } = LAYOUTS[view];
  const { data, error, isPending, hasNextPage, isFetchingNextPage, fetchNextPage } = useProjects(filters);

  const sentinelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !hasNextPage) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0]?.isIntersecting && !isFetchingNextPage) {
        void fetchNextPage();
      }
    });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  if (isPending) {
    return (
      <div aria-busy="true" className={className}>
        <span className="sr-only">Loading projects…</span>
        {Array.from({ length: FIRST_LOAD_SKELETONS }, (_, index) => (
          <ItemSkeleton key={index} />
        ))}
      </div>
    );
  }
  if (error) {
    return <p className="text-[14px]/6 text-destructive">Could not load projects: {error.message}</p>;
  }

  const projects = data.pages.flatMap((page) => page.items);
  if (projects.length === 0) {
    return <p className="text-[14px]/6 text-neutral-700">No projects match these filters.</p>;
  }

  return (
    <>
      <div aria-busy={isFetchingNextPage} className={className}>
        {projects.map((project) => (
          <Item key={project.id} project={project} />
        ))}
        {isFetchingNextPage &&
          Array.from({ length: NEXT_PAGE_SKELETONS }, (_, index) => <ItemSkeleton key={`skeleton-${index}`} />)}
      </div>
      <div ref={sentinelRef} aria-hidden className="h-px" />
    </>
  );
}
