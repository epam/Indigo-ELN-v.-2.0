import { Link } from '@tanstack/react-router';
import { NotebookText } from 'lucide-react';
import type { ReactNode } from 'react';

import { AvatarStack } from '@/components/common/avatar-stack';

import type { Notebook } from '@/lib/types/notebooks.ts';
import { formatDate } from '@/lib/utils.ts';

function Column({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col">
      <dt className="truncate text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 overflow-hidden text-[14px]/6">{children}</dd>
    </div>
  );
}

/** Four columns rather than ProjectRow's five — same `auto-fit` reflow. */
const COLUMNS_CLASS = 'grid gap-4 grid-cols-[repeat(auto-fit,minmax(160px,1fr))]';

export function NotebookRow({ item: notebook }: { item: Notebook }) {
  const openCount = notebook.experimentCountByStatus.OPEN ?? 0;

  return (
    <Link
      to="/notebooks/$id"
      params={{ id: notebook.id }}
      className="flex cursor-pointer flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4"
    >
      <header className="flex h-7 items-center gap-4">
        <NotebookText className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5 font-semibold">{notebook.name}</h3>
      </header>

      <dl className={COLUMNS_CLASS}>
        <Column label="Experiments">
          <span className="text-[16px]/6 font-semibold text-blue-400">{openCount}</span>
          <span className="h-4 w-px bg-neutral-300" />
          <span className="text-[16px]/6 font-semibold text-neutral-1000">{notebook.experimentCount}</span>
        </Column>
        <Column label="Members">
          <AvatarStack acl={notebook.acl} aclCount={notebook.aclCount} />
        </Column>
        <Column label="Last Edited">
          <span className="truncate">
            {formatDate(notebook.modifiedAt)} by {notebook.modifiedBy.displayName}
          </span>
        </Column>
        <Column label="Created">
          <span className="truncate">
            {formatDate(notebook.createdAt)} by {notebook.createdBy.displayName}
          </span>
        </Column>
      </dl>
    </Link>
  );
}
