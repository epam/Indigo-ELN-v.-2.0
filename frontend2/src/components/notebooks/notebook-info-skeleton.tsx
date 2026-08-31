import {Skeleton} from '@/components/ui/skeleton';
import {cn} from '@/lib/utils';

/** Widths only — one entry per labelled section of AboutNotebookCard. */
const SECTIONS = ['w-40', 'w-full', 'w-64'];
const MEMBERS = 5;

export function NotebookInfoSkeleton() {
  return (
    <div aria-hidden className="grid grid-cols-1 items-start gap-4 xl:grid-cols-[minmax(0,1fr)_512px]">
      <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
        <div className="flex items-center justify-between gap-4 border-b border-neutral-300 pb-3">
          <Skeleton className="h-6 w-36" />
          <Skeleton className="size-9 rounded-md" />
        </div>
        <div className="flex flex-col gap-6">
          {SECTIONS.map((value, index) => (
            <div key={index} className="flex flex-col gap-2">
              <Skeleton className="h-5 w-28" />
              <Skeleton className={cn('h-5', value)} />
            </div>
          ))}
        </div>
      </section>

      <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
        <div className="flex items-center gap-2 border-b border-neutral-300 pb-3">
          <Skeleton className="h-6 w-16" />
          <Skeleton className="size-6 rounded-full" />
        </div>
        <div className="flex flex-col gap-4">
          {Array.from({ length: MEMBERS }, (_, index) => (
            <div key={index} className="flex items-center gap-3">
              <Skeleton className="size-6 shrink-0 rounded-full" />
              <Skeleton className="h-5 w-32" />
              <Skeleton className="ml-auto h-5 w-40" />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
