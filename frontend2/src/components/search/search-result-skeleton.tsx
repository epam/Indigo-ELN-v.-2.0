import { CARD_CLASS, COLUMNS_CLASS } from '@/components/search/search-result-row';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

/** Widths only — the classes and heights are the row's own, so nothing shifts when data lands. */
const COLUMNS = [
  { label: 'w-16', value: 'w-28' },
  { label: 'w-20', value: 'w-16' },
  { label: 'w-16', value: 'w-24' },
];

/**
 * Shaped like the widest row — the experiment one, with a scheme — since a page of results
 * is a mix of types and a placeholder that is too short would shrink as the data arrives.
 */
export function SearchResultSkeleton() {
  return (
    <article aria-hidden className={CARD_CLASS}>
      <header className="flex h-7 items-center gap-4">
        <Skeleton className="size-5 shrink-0" />
        <Skeleton className="h-5 flex-1" />
      </header>

      <div className="flex items-start gap-4">
        <Skeleton className="h-[88px] w-[140px] shrink-0 rounded-md" />
        <dl className={COLUMNS_CLASS}>
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
        </dl>
      </div>
    </article>
  );
}
