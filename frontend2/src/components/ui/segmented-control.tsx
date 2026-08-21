import { Toggle } from '@base-ui/react/toggle';
import { ToggleGroup } from '@base-ui/react/toggle-group';

import { cn } from '@/lib/utils';

interface SegmentedControlProps<Value extends string> {
  value: Value;
  onValueChange: (value: Value) => void;
  options: { value: Value; label: string; icon: React.ReactNode }[];
  className?: string;
}

/**
 * Single-select strip. ToggleGroup is array-valued, so this maps to and from a
 * scalar and ignores the empty array (clicking the active item keeps it active).
 */
function SegmentedControl<Value extends string>({
  value,
  onValueChange,
  options,
  className,
}: SegmentedControlProps<Value>) {
  return (
    <ToggleGroup
      data-slot="segmented-control"
      value={[value]}
      onValueChange={(next) => {
        const [selected] = next;
        if (selected) onValueChange(selected);
      }}
      className={cn('flex overflow-clip rounded-md border border-neutral-300', className)}
    >
      {options.map((option) => (
        <Toggle
          key={option.value}
          value={option.value}
          aria-label={option.label}
          className="-mr-px flex size-10 items-center justify-center border-r border-neutral-300 bg-neutral-200 text-neutral-800 outline-none last:border-r-0 focus-visible:ring-3 focus-visible:ring-ring/50 data-[pressed]:bg-blue-100 data-[pressed]:text-blue-600"
        >
          {option.icon}
        </Toggle>
      ))}
    </ToggleGroup>
  );
}

export { SegmentedControl };
