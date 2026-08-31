import {Skeleton} from '@/components/ui/skeleton';
import {cn} from '@/lib/utils';

/** Widths only — the row count and heights mirror ExperimentCard so nothing shifts when data lands. */
const ROWS = [
  { label: 'w-20', value: 'w-40' },
  { label: 'w-16', value: 'w-36' },
];

const AVATARS = 3;

export function ExperimentCardSkeleton() {
  return (
    <article aria-hidden className="flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4">
      <header className="flex h-7 items-center gap-2">
        <Skeleton className="size-5 shrink-0" />
        <Skeleton className="h-5 flex-1" />
        {/* The badge's own width and height, so the header never resizes around it. */}
        <Skeleton className="h-[26px] w-[59px] shrink-0 rounded-md" />
        <div className="flex items-center">
          {Array.from({ length: AVATARS }, (_, index) => (
            <Skeleton key={index} className={cn('size-6 rounded-full', index > 0 && '-ml-2 ring-2 ring-card')} />
          ))}
        </div>
        <Skeleton className="size-6 shrink-0 rounded-md" />
      </header>

      {/* The scheme frame's own height — the tallest thing on the card. */}
      <Skeleton className="h-[160px] w-full rounded-md" />

      <div className="flex flex-col gap-1">
        {ROWS.map((row, index) => (
          <div key={index} className="flex h-6 items-center justify-between gap-4">
            <Skeleton className={cn('h-4', row.label)} />
            <Skeleton className={cn('h-4', row.value)} />
          </div>
        ))}
      </div>
    </article>
  );
}
