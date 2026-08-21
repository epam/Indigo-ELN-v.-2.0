import { File, Star } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { useMarkedExperiments } from '@/lib/api/experiments';
import { statusGroupOf } from '@/lib/types/common.ts';

export function StarredExperiments() {
  const { data, error, isPending } = useMarkedExperiments();

  return (
    <section className="flex flex-col gap-2 rounded-md border border-neutral-300 bg-neutral-100 p-3">
      <h2 className="flex items-center gap-2 text-[14px]/5 font-semibold">
        <Star className="size-4" />
        Starred Experiments
      </h2>
      {isPending && <p className="text-[12px]/5 text-neutral-700">Loading…</p>}
      {error && <p className="text-[12px]/5 text-destructive">Could not load starred experiments.</p>}
      {data?.length === 0 && <p className="text-[12px]/5 text-neutral-700">Nothing starred yet.</p>}
      {data?.map((experiment) => {
        const group = statusGroupOf(experiment.status);
        return (
          <div key={experiment.id} className="flex items-center gap-2">
            <File className="size-4 shrink-0" />
            <span className="flex-1 truncate text-[14px]/5 text-neutral-1000">{experiment.name}</span>
            <Badge variant={group.key}>{group.label}</Badge>
          </div>
        );
      })}
    </section>
  );
}
