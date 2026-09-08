import { createFileRoute, Outlet } from '@tanstack/react-router';

import { ProjectHeader } from '@/components/projects/details/project-header';
import { useProject } from '@/lib/api/projects';

// `projects_` keeps this off `/_auth/projects` as a child route — the projects page is a
// leaf with no <Outlet />, so the detail page has to stay its sibling.
export const Route = createFileRoute('/_auth/projects_/$id')({
  component: ProjectPage,
});

function ProjectPage() {
  const { id } = Route.useParams();
  // The tabs below call this too and are served from cache; one query, two readers.
  const { data: project } = useProject(id);

  return (
    <>
      <ProjectHeader projectId={id} project={project} />
      <Outlet />
    </>
  );
}
