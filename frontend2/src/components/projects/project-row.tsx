import { Link } from '@tanstack/react-router';
import { Briefcase } from 'lucide-react';

import { AvatarStack } from '@/components/projects/avatar-stack';

import type { Project } from '@/lib/types/projects.ts';
import { formatDate } from '@/lib/utils.ts';

function Column({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col">
      <dt className="truncate text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 overflow-hidden text-[14px]/6">{children}</dd>
    </div>
  );
}

/**
 * The card's data as one full-width row. `auto-fit` gives five equal columns when there
 * is room and drops to fewer as the row narrows — measured against the row itself, so
 * collapsing the sidebar reflows it just like resizing the window does.
 */
const COLUMNS_CLASS = 'grid gap-4 grid-cols-[repeat(auto-fit,minmax(160px,1fr))]';

export function ProjectRow({ item: project }: { item: Project }) {
  const openCount = project.experimentCountByStatus.OPEN ?? 0;

  return (
    <Link
      to="/projects/$id"
      params={{ id: project.id }}
      className="flex cursor-pointer flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4"
    >
      <header className="flex h-7 items-center gap-4">
        <Briefcase className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5 font-semibold">{project.name}</h3>
      </header>

      <dl className={COLUMNS_CLASS}>
        <Column label="Notebook">
          <span className="text-[16px]/6 font-semibold">{project.notebookCount}</span>
        </Column>
        <Column label="Experiments">
          <span className="text-[16px]/6 font-semibold text-blue-400">{openCount}</span>
          <span className="h-4 w-px bg-neutral-300" />
          <span className="text-[16px]/6 font-semibold text-neutral-1000">{project.experimentCount}</span>
        </Column>
        <Column label="Members">
          <AvatarStack acl={project.acl} aclCount={project.aclCount} />
        </Column>
        <Column label="Last Edited">
          <span className="truncate">
            {formatDate(project.modifiedAt)} by {project.modifiedBy.displayName}
          </span>
        </Column>
        <Column label="Created">
          <span className="truncate">
            {formatDate(project.createdAt)} by {project.createdBy.displayName}
          </span>
        </Column>
      </dl>
    </Link>
  );
}
