import { cva, type VariantProps } from 'class-variance-authority';

export const badgeVariants = cva(
  ['inline-flex items-center gap-1 rounded-lg text-xs text-neutral-1000 p-1'],
  {
    variants: {
      variant: {
        grey: [
          'bg-neutral-alpha-10  [&_.badge-icon]:text-neutral-500 border border-neutral-400',
        ],
        blue: [
          'bg-primary-alpha-10',
          '[&_.badge-icon]:text-primary-400 border border-primary-400',
        ],
        violet: [
          'bg-violet-alpha-10',
          '[&_.badge-icon]:text-violet-200 border border-violet-200',
        ],
        green: [
          'bg-green-alpha-10',
          '[&_.badge-icon]:text-green-200 border border-green-200',
        ],
        red: [
          'bg-red-alpha-10',
          '[&_.badge-icon]:text-red-200 border border-red-200',
        ],
        yellow: [
          'bg-yellow-alpha-10',
          '[&_.badge-icon]:text-yellow-200 border border-yellow-200',
        ],
      },
      size: {
        default: 'text-[14px]',
      },
    },
    defaultVariants: {
      variant: 'blue',
      size: 'default',
    },
  },
);

export type BadgeVariantProps = VariantProps<typeof badgeVariants>;
