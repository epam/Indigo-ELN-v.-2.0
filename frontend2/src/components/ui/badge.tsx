import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

const badgeVariants = cva(
  'inline-flex w-[59px] cursor-default items-center gap-1 rounded-md border px-2 py-0.5 text-[12px]/5 text-neutral-1000',
  {
    variants: {
      // Keys are the ExperimentStatus values verbatim, so a status can be passed straight through.
      variant: {
        OPEN: 'border-blue-400 bg-blue-10',
        REOPEN: 'border-blue-600 bg-blue-600-10',
        COMPLETED: 'border-violet-200 bg-violet-10',
        SIGNING: 'border-orange-200 bg-orange-10',
        SUBMITTED: 'border-orange-400 bg-orange-400-10',
        SIGNED: 'border-violet-400 bg-violet-400-10',
        REJECTED: 'border-red-200 bg-red-10',
        CANCELLED: 'border-neutral-300 bg-neutral-100',
        ARCHIVED: 'border-green-200 bg-green-10',
      },
    },
    defaultVariants: {
      variant: 'OPEN',
    },
  },
);

const dotVariants = cva('size-1.5 shrink-0 rounded-full', {
  variants: {
    variant: {
      OPEN: 'bg-blue-400',
      REOPEN: 'bg-blue-600',
      COMPLETED: 'bg-violet-200',
      SIGNING: 'bg-orange-200',
      SUBMITTED: 'bg-orange-400',
      SIGNED: 'bg-violet-400',
      REJECTED: 'bg-red-200',
      CANCELLED: 'bg-neutral-700',
      ARCHIVED: 'bg-green-200',
    },
  },
  defaultVariants: {
    variant: 'OPEN',
  },
});

type BadgeProps = React.ComponentProps<'span'> & VariantProps<typeof badgeVariants>;

function Badge({ className, variant = 'OPEN', children, ...props }: BadgeProps) {
  return (
    <span data-slot="badge" className={cn(badgeVariants({ variant, className }))} {...props}>
      <span className={cn(dotVariants({ variant }))} />
      <span className="truncate">{children}</span>
    </span>
  );
}

export { Badge, badgeVariants };
