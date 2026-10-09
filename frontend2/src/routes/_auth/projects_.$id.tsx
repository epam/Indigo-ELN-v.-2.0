import { createFileRoute, Outlet, useMatch } from '@tanstack/react-router';

import { ProjectHeader } from '@/components/projects/details/project-header';
import { useProject } from '@/lib/api/projects';
import { usePageTitle } from '@/lib/hooks/use-page-title';

// `projects_` keeps this off `/_auth/projects` as a child route — the projects page is a
// leaf with no <Outlet />, so the detail page has to stay its sibling.
export const Route = createFileRoute('/_auth/projects_/$id')({
  component: ProjectPage,
});

function ProjectPage() {
  const { id } = Route.useParams();
  // The tabs below call this too and are served from cache; one query, two readers.
  const { data: project } = useProject(id);
  // Set here rather than in the tabs: child effects run first, so two callers would fight.
  const onNotebooks = useMatch({ from: '/_auth/projects_/$id/notebooks', shouldThrow: false });
  usePageTitle(project && `Project ${project.name}`, onNotebooks && 'Notebooks');

  return (
    <>
      <ProjectHeader projectId={id} project={project} />
      <Outlet />
    </>
  );
}
