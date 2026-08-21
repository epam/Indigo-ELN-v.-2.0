import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

const badgeVariants = cva(
  'inline-flex w-[59px] items-center gap-1 rounded-2 border px-2 py-0.5 text-[12px]/5 text-neutral-1000',
  {
    variants: {
      variant: {
        open: 'border-blue-400 bg-blue-10',
        completed: 'border-violet-200 bg-violet-10',
        signing: 'border-orange-200 bg-orange-10',
        rejected: 'border-red-200 bg-red-10',
        cancelled: 'border-neutral-300 bg-neutral-100',
        archived: 'border-green-200 bg-green-10',
      },
    },
    defaultVariants: {
      variant: 'open',
    },
  },
);

const dotVariants = cva('size-1.5 shrink-0 rounded-full', {
  variants: {
    variant: {
      open: 'bg-blue-400',
      completed: 'bg-violet-200',
      signing: 'bg-orange-200',
      rejected: 'bg-red-200',
      cancelled: 'bg-neutral-700',
      archived: 'bg-green-200',
    },
  },
  defaultVariants: {
    variant: 'open',
  },
});

type BadgeProps = React.ComponentProps<'span'> & VariantProps<typeof badgeVariants>;

function Badge({ className, variant = 'open', children, ...props }: BadgeProps) {
  return (
    <span data-slot="badge" className={cn(badgeVariants({ variant, className }))} {...props}>
      <span className={cn(dotVariants({ variant }))} />
      <span className="truncate">{children}</span>
    </span>
  );
}

export { Badge, badgeVariants };
