import { Tabs as TabsPrimitive } from '@base-ui/react/tabs';

import { cn } from '@/lib/utils';

/**
 * The app's underlined tab strip, as a real tablist.
 *
 * The page-level strips (`ExperimentHeader`, `ProjectHeader`) are `<Link>`s driven by a search
 * param and need no roving focus — the browser walks links already. This is for tabs *inside* a
 * surface, where the selection is component state and the arrow-key contract, `aria-controls`
 * and the panel wiring all have to be provided.
 *
 * The visual is deliberately the same three-part one those strips hard-code, so a tab reads as a
 * tab wherever it appears.
 *
 * `TabsPanel` does not keep its content mounted — Base UI's default, and relied on: a panel that
 * has never been shown has never mounted, so a query inside it has never run.
 */
function Tabs({ className, ...props }: TabsPrimitive.Root.Props) {
  return <TabsPrimitive.Root data-slot="tabs" className={cn('flex min-h-0 flex-col gap-4', className)} {...props} />;
}

function TabsList({ className, ...props }: TabsPrimitive.List.Props) {
  return (
    <TabsPrimitive.List
      data-slot="tabs-list"
      className={cn('flex shrink-0 items-center gap-6 border-b border-neutral-300', className)}
      {...props}
    />
  );
}

function TabsTab({ className, ...props }: TabsPrimitive.Tab.Props) {
  return (
    <TabsPrimitive.Tab
      data-slot="tabs-tab"
      className={cn(
        // -1px so the active underline sits on the list's own border rather than above it.
        'mb-[-1px] flex cursor-pointer items-center gap-1 border-b-2 px-2 pb-2 text-[14px]/6 outline-none',
        'border-transparent text-neutral-800',
        'hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50',
        'disabled:cursor-not-allowed disabled:opacity-50',
        'data-active:border-blue-400 data-active:font-semibold data-active:text-blue-400',
        className,
      )}
      {...props}
    />
  );
}

function TabsPanel({ className, ...props }: TabsPrimitive.Panel.Props) {
  return (
    <TabsPrimitive.Panel
      data-slot="tabs-panel"
      className={cn('flex min-h-0 flex-1 flex-col outline-none', className)}
      {...props}
    />
  );
}

export { Tabs, TabsList, TabsTab, TabsPanel };
