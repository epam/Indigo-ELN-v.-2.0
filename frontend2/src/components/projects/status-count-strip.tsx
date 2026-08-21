import { STATUS_GROUPS, sumStatuses } from '@/lib/types/common.ts';
import { cn } from '@/lib/utils';
import type { TotalCounts } from '@/lib/types/projects.ts';

export function StatusCountStrip({ counts }: { counts: TotalCounts | undefined }) {
  return (
    <div className="flex w-[600px] overflow-clip rounded-lg border border-neutral-300">
      {STATUS_GROUPS.map((group) => (
        <div
          key={group.key}
          className="-mr-px flex flex-1 flex-col gap-1 border-r border-neutral-300 bg-card px-3 py-2 last:border-r-0"
        >
          <span className={cn('text-[18px]/7 font-semibold', group.colorClass)}>
            {counts ? sumStatuses(counts.experimentsByStatus, group.statuses) : '—'}
          </span>
          <span className="truncate text-[14px]/6 text-neutral-1000">{group.label}</span>
        </div>
      ))}
    </div>
  );
}
