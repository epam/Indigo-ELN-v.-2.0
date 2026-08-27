import { Input as InputPrimitive } from '@base-ui/react/input';

import { cn } from '@/lib/utils';

function Input({ className, ...props }: InputPrimitive.Props) {
  return (
    <InputPrimitive
      data-slot="input"
      className={cn(
        'h-10 w-full rounded-md border border-neutral-300 bg-background px-3 text-[14px]/6 text-neutral-1000 outline-none transition-colors',
        'placeholder:text-neutral-700 focus-visible:border-blue-400 focus-visible:ring-3 focus-visible:ring-ring/20',
        'disabled:cursor-not-allowed disabled:opacity-50 data-[invalid]:border-red-200 data-[invalid]:ring-red-200/20',
        className,
      )}
      {...props}
    />
  );
}

export { Input };
