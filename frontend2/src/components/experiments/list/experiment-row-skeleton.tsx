import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

/** Widths only — the column count and heights mirror ExperimentRow so nothing shifts when data lands. */
const COLUMNS = [
  { label: 'w-16', value: null },
  { label: 'w-20', value: 'w-32' },
  { label: 'w-16', value: 'w-28' },
];

const AVATARS = 3;

export function ExperimentRowSkeleton() {
  return (
    <article aria-hidden className="flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4">
      <header className="flex h-7 items-center gap-2">
        <Skeleton className="size-5 shrink-0" />
        <Skeleton className="h-5 flex-1" />
        <Skeleton className="h-[26px] w-[59px] shrink-0 rounded-md" />
        <Skeleton className="size-6 shrink-0 rounded-md" />
      </header>

      <div className="flex items-start gap-4">
        <Skeleton className="h-[88px] w-[140px] shrink-0 rounded-md" />

        <div className="grid min-w-0 flex-1 grid-cols-[repeat(auto-fit,minmax(160px,1fr))] gap-4">
          {COLUMNS.map((column, index) => (
            <div key={index} className="flex min-w-0 flex-col">
              <div className="flex h-6 items-center">
                <Skeleton className={cn('h-4', column.label)} />
              </div>
              <div className="flex h-6 items-center">
                {column.value ? (
                  <Skeleton className={cn('h-4', column.value)} />
                ) : (
                  Array.from({ length: AVATARS }, (_, avatar) => (
                    <Skeleton
                      key={avatar}
                      className={cn('size-6 rounded-full', avatar > 0 && '-ml-2 ring-2 ring-card')}
                    />
                  ))
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </article>
  );
}
