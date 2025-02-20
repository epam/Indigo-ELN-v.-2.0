import { cva, type VariantProps } from 'class-variance-authority';

export const chipVariants = cva(
  [
    '[&:not(:has(.chip-icon))]:px-3 py-1',
    '[&:has(.chip-icon)]:pl-3 [&:has(.chip-icon)]:pr-2',
    'inline-flex items-center gap-1 rounded-full text-xs text-neutral-1000',
    '[&_.chip-icon]:cursor-pointer',
    '[&_.chip-icon]:p-[0.5px]',
    '[&_.chip-icon]:hover:bg-primary-alpha-10',
    '[&_.chip-icon]:hover:rounded-xs',
  ],
  {
    variants: {
      active: {
        true: ['bg-primary-100'],
        false: ['bg-neutral-300'],
      },
      error: {
        true: ['bg-red-50'],
      },
      size: {
        default: 'text-[14px]',
      },
    },
    defaultVariants: {
      active: true,
      size: 'default',
    },
  },
);

export type ChipVariantProps = VariantProps<typeof chipVariants>;
