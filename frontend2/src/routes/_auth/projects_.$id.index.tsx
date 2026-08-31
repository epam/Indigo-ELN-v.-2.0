import {createFileRoute} from '@tanstack/react-router';

import {AboutProjectCard} from '@/components/projects/about-project-card';
import {ProjectInfoSkeleton} from '@/components/projects/project-info-skeleton';
import {TeamCard} from '@/components/projects/team-card';
import {useProject} from '@/lib/api/projects';

export const Route = createFileRoute('/_auth/projects_/$id/')({
  component: ProjectInfoTab,
});

/**
 * About and Team side by side. The two columns are a fixed 1fr / 512px split down to the
 * point where Team no longer fits, then stack — matching the design's proportions without
 * pinning the About card to a width.
 */
const COLUMNS_CLASS = 'grid items-start gap-4 grid-cols-1 xl:grid-cols-[minmax(0,1fr)_512px]';

function ProjectInfoTab() {
  const { id } = Route.useParams();
  const { data: project, isPending, isError } = useProject(id);

  if (isPending) return <ProjectInfoSkeleton />;
  // apiFetch has already toasted the failure; this is the page saying what it cannot show.
  if (isError || !project) {
    return <p className="text-[14px]/6 text-destructive">This project could not be loaded.</p>;
  }

  return (
    <div className={COLUMNS_CLASS}>
      <AboutProjectCard project={project} />
      <TeamCard project={project} />
    </div>
  );
}
