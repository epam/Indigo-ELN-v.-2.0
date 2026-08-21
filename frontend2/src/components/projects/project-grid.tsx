import { useEffect, useRef } from 'react';

import { ProjectCard } from '@/components/projects/project-card';
import { useProjects } from '@/lib/api/projects';
import type { ProjectFilters } from '@/lib/types/projects';

export function ProjectGrid({ filters }: { filters: ProjectFilters }) {
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
    return <p className="text-[14px]/6 text-neutral-700">Loading projects…</p>;
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
      <div className="grid grid-cols-3 gap-3">
        {projects.map((project) => (
          <ProjectCard key={project.id} project={project} />
        ))}
      </div>
      <div ref={sentinelRef} aria-hidden className="h-px" />
      {isFetchingNextPage && <p className="text-[14px]/6 text-neutral-700">Loading more projects…</p>}
    </>
  );
}
