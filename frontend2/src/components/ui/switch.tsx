import { Switch as SwitchPrimitive } from '@base-ui/react/switch';

import { cn } from '@/lib/utils';

function Switch({ className, ...props }: SwitchPrimitive.Root.Props) {
  return (
    <SwitchPrimitive.Root
      data-slot="switch"
      className={cn(
        'inline-flex h-5 w-9 shrink-0 items-center rounded-full bg-neutral-300 p-0.5 transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50 data-checked:bg-blue-400 data-disabled:opacity-50',
        className,
      )}
      {...props}
    >
      <SwitchPrimitive.Thumb className="size-4 rounded-full bg-white transition-transform data-checked:translate-x-4" />
    </SwitchPrimitive.Root>
  );
}

export { Switch };
