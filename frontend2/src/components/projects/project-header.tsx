import {Link} from '@tanstack/react-router';
import {Plus} from 'lucide-react';

import {Breadcrumbs} from '@/components/layout/breadcrumbs';
import {StatTileGroup} from '@/components/common/stat-tile-group';
import {StatusCountStrip} from '@/components/common/status-count-strip';
import {Button} from '@/components/ui/button';

import type {ProjectDetails} from '@/lib/types/projects.ts';

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
        {/*
          TODO(add-notebook-dialog): the create-notebook form is a separate task. The button is
          part of the header in the design, so it is rendered but stays disabled — and there is
          no CREATE_NOTEBOOKS check yet, because there is nothing behind it to gate.
        */}
        <Button size="lg" className="rounded-md" disabled>
          <Plus />
          Add Notebook
        </Button>
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
