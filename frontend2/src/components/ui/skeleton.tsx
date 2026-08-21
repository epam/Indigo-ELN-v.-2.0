import { cn } from '@/lib/utils';

function Skeleton({ className, ...props }: React.ComponentProps<'div'>) {
  // neutral-300 rather than the shadcn default `bg-muted`, which is invisible on a white card.
  return <div data-slot="skeleton" className={cn('animate-pulse rounded-2 bg-neutral-300', className)} {...props} />;
}

export { Skeleton };
