import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

/** Widths only — the column count and heights mirror DictionaryRow so nothing shifts when data lands. */
const COLUMNS = [
  { label: 'w-20', value: 'w-40' },
  { label: 'w-24', value: 'w-24' },
  { label: 'w-24', value: 'w-28' },
];

export function DictionaryRowSkeleton() {
  return (
    <article aria-hidden className="flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4">
      <header className="flex h-7 items-center gap-4">
        <Skeleton className="size-5 shrink-0" />
        <Skeleton className="h-5 flex-1" />
      </header>

      <div className="grid grid-cols-[repeat(auto-fit,minmax(160px,1fr))] gap-4">
        {COLUMNS.map((column, index) => (
          <div key={index} className="flex min-w-0 flex-col">
            <div className="flex h-6 items-center">
              <Skeleton className={cn('h-4', column.label)} />
            </div>
            <div className="flex h-6 items-center">
              <Skeleton className={cn('h-4', column.value)} />
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}
