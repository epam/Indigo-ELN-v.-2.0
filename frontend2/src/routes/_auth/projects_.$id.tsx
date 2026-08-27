import { createFileRoute } from '@tanstack/react-router';

import { Breadcrumbs } from '@/components/layout/breadcrumbs';

// `projects_` keeps this off `/_auth/projects` as a child route — the projects page is a
// leaf with no <Outlet />, so the detail page has to stay its sibling.
export const Route = createFileRoute('/_auth/projects_/$id')({
  component: ProjectPage,
});

function ProjectPage() {
  const { id } = Route.useParams();

  return (
    <>
      <section className="rounded-6 bg-card p-4 shadow-card">
        <Breadcrumbs
          items={[
            { label: 'All Projects', link: { to: '/projects' } },
            // TODO: becomes `Project: ${project.name}` once this page loads the project.
            { label: 'Project:' },
          ]}
        />
      </section>
      <h1 className="text-[16px]/6 font-semibold">Project {id} — coming soon</h1>
    </>
  );
}
