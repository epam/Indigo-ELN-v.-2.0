import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

/** Two signature lines — the common shape, an author and a witness. */
const SIGNATURE_LINES = ['w-40', 'w-48'];

/** Widths only; the column tracks and heights mirror SignatureRow so nothing shifts when data lands. */
export function SignatureRowSkeleton() {
  return (
    <article aria-hidden className="flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4">
      <header className="flex h-7 items-center gap-4">
        <Skeleton className="size-5 shrink-0" />
        <Skeleton className="h-5 flex-1" />
        <Skeleton className="h-5 w-32 shrink-0" />
      </header>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-[2fr_1fr_1fr]">
        <div className="flex min-w-0 flex-col">
          <div className="flex h-6 items-center">
            <Skeleton className="h-4 w-20" />
          </div>
          {SIGNATURE_LINES.map((width) => (
            <div key={width} className="flex h-6 items-center">
              <Skeleton className={cn('h-4', width)} />
            </div>
          ))}
        </div>

        {['w-20', 'w-16'].map((label) => (
          <div key={label} className="flex min-w-0 flex-col">
            <div className="flex h-6 items-center">
              <Skeleton className={cn('h-4', label)} />
            </div>
            <div className="flex h-6 items-center">
              <Skeleton className="h-4 w-24" />
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}
