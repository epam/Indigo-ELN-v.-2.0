import { Link } from '@tanstack/react-router';
import { FileText } from 'lucide-react';
import type { ReactNode } from 'react';

import { ApiImage } from '@/components/common/api-image';
import { AvatarStack } from '@/components/common/avatar-stack';
import { StarButton } from '@/components/experiments/star-button';
import { Badge } from '@/components/ui/badge';

import { experimentPicturePath } from '@/lib/api/experiments';
import type { Experiment } from '@/lib/types/experiments.ts';
import { EXPERIMENT_STATUS_DISPLAY } from '@/lib/types/experiments.ts';
import { formatDate } from '@/lib/utils.ts';

function Column({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col">
      <dt className="truncate text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 overflow-hidden text-[14px]/6">{children}</dd>
    </div>
  );
}

/** Same `auto-fit` reflow as NotebookRow, alongside the thumbnail SearchResultRow uses. */
const COLUMNS_CLASS = 'grid min-w-0 flex-1 gap-4 grid-cols-[repeat(auto-fit,minmax(160px,1fr))]';

export function ExperimentRow({ item: experiment }: { item: Experiment }) {
  return (
    <Link
      to="/experiments/$id"
      params={{ id: experiment.id }}
      className="flex cursor-pointer flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4"
    >
      <header className="flex h-7 items-center gap-2">
        <FileText className="size-5 shrink-0 text-neutral-800" />
        <h3 className="min-w-0 flex-1 truncate text-[14px]/5">
          <span className="text-neutral-800">Experiment </span>
          <span className="font-semibold">{experiment.name}</span>
        </h3>
        <Badge variant={experiment.status} className="w-auto shrink-0">
          {EXPERIMENT_STATUS_DISPLAY[experiment.status]}
        </Badge>
        <StarButton experimentId={experiment.id} marked={experiment.marked} />
      </header>

      <div className="flex items-start gap-4">
        <ApiImage
          path={experimentPicturePath(experiment.id, experiment.revision)}
          alt="Reaction scheme"
          className="h-[88px] w-[140px]"
        />

        <dl className={COLUMNS_CLASS}>
          <Column label="Members">
            <AvatarStack acl={experiment.acl} aclCount={experiment.aclCount} />
          </Column>
          <Column label="Last Edited">
            <span className="truncate">
              {formatDate(experiment.modifiedAt)} by {experiment.modifiedBy.displayName}
            </span>
          </Column>
          <Column label="Created">
            <span className="truncate">
              {formatDate(experiment.createdAt)} by {experiment.createdBy.displayName}
            </span>
          </Column>
        </dl>
      </div>
    </Link>
  );
}
