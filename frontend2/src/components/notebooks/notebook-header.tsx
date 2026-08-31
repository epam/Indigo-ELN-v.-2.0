import {Link} from '@tanstack/react-router';
import {Plus} from 'lucide-react';

import {Breadcrumbs} from '@/components/layout/breadcrumbs';
import {StatTileGroup} from '@/components/common/stat-tile-group';
import {StatusCountStrip} from '@/components/common/status-count-strip';
import {Button} from '@/components/ui/button';

import type {NotebookDetails} from '@/lib/types/notebooks.ts';

// Split across the three props rather than merged with `cn()` — see ProjectHeader for why.
const TAB_CLASS = 'border-b-2 px-2 pb-2 text-[14px]/6';
const TAB_ACTIVE_CLASS = 'border-blue-400 font-semibold text-blue-400';
const TAB_INACTIVE_CLASS = 'border-transparent text-neutral-800';

/**
 * The card above both notebook tabs: trail, actions, counts, and the tab strip itself.
 * `notebook` is undefined until the detail query resolves — the counts show skeletons and the
 * trail its bare labels, so the header never changes height as data lands.
 *
 * The trail names the parent project from `projectId`/`projectName` on the payload: the URL is
 * flat (`/notebooks/{id}`) and never carries the project, so there is nothing else to read and
 * no second request to make.
 */
export function NotebookHeader({
  notebookId,
  notebook,
}: {
  notebookId: string;
  notebook: NotebookDetails | undefined;
}) {
  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-4">
        <Breadcrumbs
          items={[
            { label: 'All Projects', link: { to: '/projects' } },
            {
              label: notebook ? `Project: ${notebook.projectName}` : 'Project:',
              link: notebook ? { to: '/projects/$id', params: { id: notebook.projectId } } : undefined,
            },
            { label: notebook ? `Notebook: ${notebook.name}` : 'Notebook:' },
          ]}
          className="min-w-0 flex-1"
        />
        {/*
          TODO(add-experiment-dialog): creating an experiment needs a template picked from
          /templates, which is a separate task. The button is part of the header in the design,
          so it is rendered but stays disabled — and there is no CREATE_EXPERIMENTS check yet,
          because there is nothing behind it to gate.
        */}
        <Button size="lg" className="rounded-md" disabled>
          <Plus />
          Add Experiment
        </Button>
      </div>

      <div className="flex items-center justify-between gap-4">
        <StatTileGroup tiles={[{ key: 'experiments', count: notebook?.experimentCount }]} />
        <StatusCountStrip counts={notebook?.experimentCountByStatus} />
      </div>

      <nav className="-mb-4 flex gap-2 border-b border-neutral-300">
        {/* exact: without it Notebook Info stays active on /experiments, which is nested under it. */}
        <Link
          to="/notebooks/$id"
          params={{ id: notebookId }}
          activeOptions={{ exact: true }}
          className={TAB_CLASS}
          activeProps={{ className: TAB_ACTIVE_CLASS }}
          inactiveProps={{ className: TAB_INACTIVE_CLASS }}
        >
          Notebook Info
        </Link>
        <Link
          to="/notebooks/$id/experiments"
          params={{ id: notebookId }}
          className={TAB_CLASS}
          activeProps={{ className: TAB_ACTIVE_CLASS }}
          inactiveProps={{ className: TAB_INACTIVE_CLASS }}
        >
          Experiments
        </Link>
      </nav>
    </section>
  );
}
