import { Link } from '@tanstack/react-router';
import { File, Star } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useMarkedExperiments } from '@/lib/api/experiments';
import { EXPERIMENT_STATUS_DISPLAY } from '@/lib/types/experiments.ts';

const SKELETON_ROWS = 3;

export function StarredExperiments() {
  const { data, error, isPending } = useMarkedExperiments();

  return (
    <section className="flex flex-col gap-2 rounded-md border border-neutral-300 bg-neutral-100 p-3">
      <h2 className="flex items-center gap-2 text-[14px]/5 font-semibold">
        <Star className="size-4" />
        Starred Experiments
      </h2>
      {isPending && (
        <div aria-busy="true" className="flex flex-col gap-2">
          <span className="sr-only">Loading starred experiments…</span>
          {Array.from({ length: SKELETON_ROWS }, (_, index) => (
            // h-[26px] is the badge's own height, which sets the height of a real row.
            <div key={index} className="flex h-6.5 items-center gap-2">
              <Skeleton className="size-4 shrink-0" />
              <Skeleton className="h-4 flex-1" />
              <Skeleton className="h-6.5 w-14.75 shrink-0 rounded-md" />
            </div>
          ))}
        </div>
      )}
      {error && <p className="text-[12px]/5 text-destructive">Could not load starred experiments.</p>}
      {data?.length === 0 && <p className="text-[12px]/5 text-neutral-700">Nothing starred yet.</p>}
      {data?.map((experiment) => (
        <Link
          key={experiment.id}
          to="/experiments/$id"
          params={{ id: experiment.id }}
          className="flex cursor-pointer items-center gap-2 rounded-2 hover:bg-neutral-200"
        >
          <File className="size-4 shrink-0" />
          <span className="flex-1 truncate text-[14px]/5 text-neutral-1000">{experiment.name}</span>
          {/* The badge is fixed-width, so title carries the label the truncation hides. */}
          <Badge variant={experiment.status} title={EXPERIMENT_STATUS_DISPLAY[experiment.status]}>
            {EXPERIMENT_STATUS_DISPLAY[experiment.status]}
          </Badge>
        </Link>
      ))}
    </section>
  );
}
