import { cva, type VariantProps } from 'class-variance-authority';

export const ButtonVariant = cva(
  [
    'text-[14px] leading-[24px] tracking-[0]',
    'py-[8px] px-[12px]',
    'cursor-pointer',
    'inline-flex items-center justify-center rounded-lg transition-colors',
    'focus:ring-2 focus:ring-primary-500',
    'disabled:pointer-events-none disabled:bg-neutral-200',
    '[&_em:first-child]:mr-2',
  ],
  {
    variants: {
      variant: {
        green: ['bg-green-400', 'hover:bg-green-300'],
        blue: ['bg-primary-400', 'hover:bg-primary-500', 'text-neutral'],
        'blue-outline': [
          'bg-neutral',
          'text-primary-400',
          'hover:bg-primary-50',
          'border border-primary-400',
          'focus:bg-neutral-200',
          'disabled:border-neutral-400 disabled:text-neutral-500 disabled:bg-neutral',
        ],
        grey: [
          'bg-neutral-200',
          'text-primary-400',
          'hover:bg-primary-50',
          'focus:bg-neutral-200',
          'disabled:bg-neutral-200 disabled:text-neutral-500',
        ],
        'grey-outline': [
          'bg-neutral',
          'border-neutral-400',
          'text-primary-400',
          'hover:bg-neutral-100',
          'disabled:border-neutral-400 disabled:text-neutral-500 disabled:bg-neutral',
        ],
        red: ['bg-red-200 hover:bg-red-300', 'text-neutral'],
        'red-outline': [
          'bg-neutral',
          'border border-red-200',
          'text-red-200',
          'hover:bg-red-alpha-10',
          'disabled:border-neutral-400 disabled:text-neutral-500 disabled:bg-neutral',
        ],
      },
      fullWidth: {
        true: 'w-full',
      },
    },
    defaultVariants: {
      variant: 'green',
    },
  },
);

export type ButtonVariants = VariantProps<typeof ButtonVariant>;
