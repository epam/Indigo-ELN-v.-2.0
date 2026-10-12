import { ChevronDown } from 'lucide-react';
import type { ChangeEvent } from 'react';

import { Button } from '@/components/ui/button';
import { INPUT_BOX, INPUT_BOX_FOCUS_WITHIN, INPUT_DISABLED } from '@/components/ui/input';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import { cn } from '@/lib/utils';

/**
 * An operator picker and a number in one box: "≥ 90", "> 5".
 *
 * Only the box. Which operators there are and what an operator without a number means is the
 * caller's — `NumericSearchField` emits nothing until there is a number, the solubility editor
 * keeps the two apart.
 */
function OperatorNumberInput<T extends string>({
  operators,
  operatorLabels,
  operator,
  onOperatorChange,
  value,
  onChange,
  id,
  label,
  'aria-label': ariaLabel,
  placeholder,
  disabled,
}: {
  operators: readonly T[];
  operatorLabels: Record<T, string>;
  operator: T;
  onOperatorChange: (operator: T) => void;
  value: number | string;
  onChange: (event: ChangeEvent<HTMLInputElement>) => void;
  id?: string;
  /** Names the operator button for screen readers, e.g. "Batch Yield, % operator". */
  label: string;
  /** Names the number box where no visible `<label>` does. */
  'aria-label'?: string;
  placeholder?: string;
  /** Renders what is set but accepts no interaction. */
  disabled?: boolean;
}) {
  return (
    <div
      className={cn(
        INPUT_BOX,
        INPUT_BOX_FOCUS_WITHIN,
        // `items-stretch`, so the operator button is a full-height segment; the padding is on
        // the inner inputs rather than on the shell.
        'flex h-10 items-stretch',
        disabled && INPUT_DISABLED,
      )}
    >
      <Menu>
        <MenuTrigger
          render={
            <Button
              type="button"
              variant="ghost"
              disabled={disabled}
              aria-label={`${label} operator`}
              className="h-auto w-16 shrink-0 justify-between rounded-none rounded-l-md border-r border-neutral-300 px-2 text-[14px]/6 text-neutral-1000"
            >
              {operatorLabels[operator]}
              <ChevronDown className="text-neutral-700" />
            </Button>
          }
        />
        <MenuContent align="start" className="min-w-16">
          {operators.map((option) => (
            <MenuItem key={option} onClick={() => onOperatorChange(option)}>
              {operatorLabels[option]}
            </MenuItem>
          ))}
        </MenuContent>
      </Menu>
      <input
        id={id}
        type="number"
        aria-label={ariaLabel}
        value={value}
        placeholder={placeholder}
        disabled={disabled}
        onChange={onChange}
        className="min-w-0 flex-1 bg-transparent px-3 text-[14px]/6 text-neutral-1000 outline-none placeholder:text-neutral-700 disabled:cursor-not-allowed"
      />
    </div>
  );
}

export { OperatorNumberInput };
