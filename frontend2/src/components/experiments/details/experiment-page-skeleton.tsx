import { Skeleton } from '@/components/ui/skeleton';

/**
 * What the experiment screen shows while its detail loads. `ExperimentHeader` renders its own
 * placeholders once the header has an id to work with, so this is the whole-page first paint —
 * before either query has answered and there is nothing at all to frame.
 */
export function ExperimentPageSkeleton() {
  return (
    <div aria-hidden className="flex flex-col gap-4">
      <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
        <div className="flex items-center gap-4">
          <Skeleton className="my-1.5 h-6 w-96" />
          <Skeleton className="ml-auto h-6 w-16 rounded-full" />
          <Skeleton className="h-6 w-24 rounded-full" />
          <Skeleton className="h-9 w-48 rounded-md" />
        </div>
        <div className="-mb-4 flex gap-2 border-b border-neutral-300">
          {[0, 1, 2].map((index) => (
            <Skeleton key={index} className="mx-2 mb-3 h-4 w-20" />
          ))}
        </div>
      </section>

      <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
        <Skeleton className="h-6 w-56" />
        <Skeleton className="h-24 w-full" />
      </section>
    </div>
  );
}
