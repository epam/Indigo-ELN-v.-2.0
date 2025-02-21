import { cva, type VariantProps } from 'class-variance-authority';

export const counterVariants = cva(
  [
    'text-primary-500 p-1 inline-flex items-center justify-center rounded-full font-bold text-center',
    '[&.filled]:text-neutral',
    '[&.filled]:border-none',
  ],
  {
    variants: {
      variant: {
        grey: [
          'border border-neutral-400 bg-neutral-alpha-10',
          '[&.filled]:bg-neutral-800',
        ],
        blue: [
          'border border-primary-400 bg-primary-alpha-10',
          '[&.filled]:bg-primary-400',
        ],
        green: [
          'border border-green-200 bg-green-alpha-10',
          '[&.filled]:bg-green-200',
        ],
        yellow: [
          'border border-yellow-200 bg-yellow-alpha-10',
          '[&.filled]:bg-yellow-200',
        ],
        orange: [
          'border border-orange-200 bg-orange-alpha-10',
          '[&.filled]:bg-orange-200',
        ],
        red: ['border border-red-200 bg-red-alpha-10', '[&.filled]:bg-red-200'],
      },
      size: {
        default: [
          'min-w-[20px] min-h-[20px] text-[10px] leading-3 tracking-[0px]',
        ],
        large: ['min-w-[24px] min-h-[24px] text-xs leading-3 tracking-[0px]'],
      },
    },
    defaultVariants: {
      variant: 'blue',
      size: 'default',
    },
  },
);

export type CounterVariants = VariantProps<typeof counterVariants>;
