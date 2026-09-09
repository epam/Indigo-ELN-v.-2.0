import { Link } from '@tanstack/react-router';
import { Plus } from 'lucide-react';
import { useState } from 'react';

import { Breadcrumbs } from '@/components/layout/breadcrumbs';
import { NotebookFormDialog } from '@/components/notebooks/notebook-form-dialog';
import { StatTileGroup } from '@/components/common/stat-tile-group';
import { StatusCountStrip } from '@/components/common/status-count-strip';
import { Button } from '@/components/ui/button';
import { useHasPermission } from '@/lib/api/user';

import type { ProjectDetails } from '@/lib/types/projects.ts';

// Split across the three props rather than merged with `cn()`: TanStack Router concatenates
// `className` with the active/inactive one, and twMerge does not run over the join — so the two
// sets have to be disjoint or a losing `border-transparent` would still be in the list.
const TAB_CLASS = 'border-b-2 px-2 pb-2 text-[14px]/6';
const TAB_ACTIVE_CLASS = 'border-blue-400 font-semibold text-blue-400';
const TAB_INACTIVE_CLASS = 'border-transparent text-neutral-800';

/**
 * The card above both project tabs: trail, actions, counts, and the tab strip itself.
 * `project` is undefined until the detail query resolves — the counts show skeletons and the
 * trail its bare `Project:` label, so the header never changes height as data lands.
 */
export function ProjectHeader({ projectId, project }: { projectId: string; project: ProjectDetails | undefined }) {
  /*
    TODO: gate on the project's own permission once the backend ships it. The right question is
    per-project — `NotebookHandlers` does `ensureAccess(notebook.getProject(), CREATE_NOTEBOOKS)` —
    but `ProjectService.getProjectDetails` retains `currentPermissions` down to
    VIEW/EDIT/MANAGE_PROJECT_ACCESS/DELETE_PROJECTS, so the payload cannot answer it yet. Adding
    CREATE_NOTEBOOKS to that `retainAll` is all it takes; `ACLService.getCurrentPermissions`
    already computes it (AccessLevel.EDIT and above grant it). Until that is deployed the global
    permission is the closest available answer — `undefined` while `currentUser` resolves counts
    as "not yet", so the button never flickers from enabled to disabled.
  */
  // const canCreate = project?.currentPermissions.includes('CREATE_NOTEBOOKS') ?? false;
  const canCreate = useHasPermission('CREATE_NOTEBOOKS') === true;
  const [addOpen, setAddOpen] = useState(false);

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-4">
        <Breadcrumbs
          items={[
            { label: 'All Projects', link: { to: '/projects' } },
            { label: project ? `Project: ${project.name}` : 'Project:' },
          ]}
          className="min-w-0 flex-1"
        />
        <Button size="lg" className="rounded-md" disabled={!canCreate} onClick={() => setAddOpen(true)}>
          <Plus />
          Add Notebook
        </Button>
        <NotebookFormDialog open={addOpen} onOpenChange={setAddOpen} projectId={projectId} />
      </div>

      <div className="flex items-center justify-between gap-4">
        <StatTileGroup
          tiles={[
            { key: 'notebooks', count: project?.notebookCount },
            { key: 'experiments', count: project?.experimentCount },
          ]}
        />
        <StatusCountStrip counts={project?.experimentCountByStatus} />
      </div>

      <nav className="-mb-4 flex gap-2 border-b border-neutral-300">
        {/* exact: without it the Project Info tab stays active on /notebooks, which is nested under it. */}
        <Link
          to="/projects/$id"
          params={{ id: projectId }}
          activeOptions={{ exact: true }}
          className={TAB_CLASS}
          activeProps={{ className: TAB_ACTIVE_CLASS }}
          inactiveProps={{ className: TAB_INACTIVE_CLASS }}
        >
          Project Info
        </Link>
        <Link
          to="/projects/$id/notebooks"
          params={{ id: projectId }}
          className={TAB_CLASS}
          activeProps={{ className: TAB_ACTIVE_CLASS }}
          inactiveProps={{ className: TAB_INACTIVE_CLASS }}
        >
          Notebooks
        </Link>
      </nav>
    </section>
  );
}
