import type { ReactNode } from 'react';

import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import {
  EXPERIMENT_STATUS_COLOR,
  EXPERIMENT_STATUS_LABELS,
  EXPERIMENT_STATUSES,
  type ExperimentStatus,
  type ExperimentStatusCounts,
} from '@/lib/types/experiments.ts';

/** Shown on its own while counts load and when every status is zero. */
const FALLBACK_STATUS: ExperimentStatus = 'OPEN';

function Cell({ status, children }: { status: ExperimentStatus; children: ReactNode }) {
  return (
    <div className="-mr-px flex w-25 shrink-0 flex-col gap-1 border-neutral-300 bg-card px-3 py-2 not-last:border-r">
      {children}
      <span className="truncate text-[14px]/6 text-neutral-1000">{EXPERIMENT_STATUS_LABELS[status]}</span>
    </div>
  );
}

export function StatusCountStrip({ counts }: { counts: ExperimentStatusCounts | undefined }) {
  // Zero-count statuses are dropped, so the strip is as wide as the data needs.
  const visible = counts ? EXPERIMENT_STATUSES.filter((status) => (counts[status] ?? 0) > 0) : [];

  return (
    // min-w-0 lets the strip shrink past its cells (overflow-clip alone does not
    // zero a flex item's automatic minimum size), and shrink-0 keeps the cells at
    // full width so the surplus is clipped rather than squeezed.
    <div className="flex min-w-0 overflow-clip rounded-lg border border-neutral-300">
      {!counts && (
        <Cell status={FALLBACK_STATUS}>
          {/* my-1 keeps the 28px line box of the real count, so the cell never resizes. */}
          <Skeleton className="my-1 h-5 w-8" />
        </Cell>
      )}
      {counts && visible.length === 0 && (
        <Cell status={FALLBACK_STATUS}>
          <span className={cn('text-[18px]/7 font-semibold', EXPERIMENT_STATUS_COLOR[FALLBACK_STATUS])}>0</span>
        </Cell>
      )}
      {counts &&
        visible.map((status) => (
          <Cell key={status} status={status}>
            <span className={cn('text-[18px]/7 font-semibold', EXPERIMENT_STATUS_COLOR[status])}>{counts[status]}</span>
          </Cell>
        ))}
    </div>
  );
}
