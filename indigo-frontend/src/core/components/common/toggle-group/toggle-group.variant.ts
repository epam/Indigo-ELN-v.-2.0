import { cva } from 'class-variance-authority';

export const buttonGroup = cva(['inline-flex rounded-lg overflow-hidden'], {
  variants: {
    variant: {
      default: 'bg-transparent border border-primary-400',
      seamless: 'bg-neutral-200',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

export const button = cva(
  [
    'flex items-center justify-center min-w-[40px] h-[40px]',
    'transition-colors cursor-pointer',
    'disabled:opacity-50 disabled:cursor-not-allowed',
  ],
  {
    variants: {
      variant: {
        default: [
          '[&:not(:last-child)]:border-r',
          'bg-transparent',
          'text-primary-400',
          'hover:bg-primary-100',
          'data-[selected=true]:bg-primary-100',
        ],
        seamless: [
          'text-primary-400',
          'hover:bg-neutral-100',
          'data-[selected=true]:bg-primary-100',
        ],
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);
