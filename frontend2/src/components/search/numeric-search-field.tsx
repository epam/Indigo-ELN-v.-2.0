import { ChevronDown } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import type { NumericSearch, NumericSearchOperator } from '@/lib/types/search.ts';
import { NUMERIC_SEARCH_OPERATOR_LABELS, NUMERIC_SEARCH_OPERATORS } from '@/lib/types/search.ts';

/**
 * An operator picker and a number, as one control: "≥ 90".
 *
 * A search with no number is no search at all, so the pair is emitted as `null` until the
 * box holds one — the same contract as indigo-frontend's NumericSearchComponent, which
 * emits null while `value == null`. The operator therefore has to survive locally in the
 * meantime: picked on an empty box it changes nothing upstream, but the button must still
 * show what was picked.
 */
function NumericSearchField({
  value,
  onValueChange,
  id,
  label,
}: {
  value: NumericSearch | null;
  onValueChange: (value: NumericSearch | null) => void;
  id: string;
  /** Names the operator button for screen readers, e.g. "Batch Yield, % operator". */
  label: string;
}) {
  const [pendingOperator, setPendingOperator] = useState<NumericSearchOperator>('eq');
  // The committed value wins whenever there is one; otherwise the button shows the choice
  // waiting for a number.
  const operator = value?.type ?? pendingOperator;

  function handleOperatorChange(next: NumericSearchOperator) {
    setPendingOperator(next);
    if (value) onValueChange({ ...value, type: next });
  }

  function handleNumberChange(event: React.ChangeEvent<HTMLInputElement>) {
    // valueAsNumber is NaN for '' and for a half-typed '-' or '1e', all of which mean
    // "no filter yet" rather than a number to send.
    const parsed = event.target.valueAsNumber;
    onValueChange(Number.isNaN(parsed) ? null : { type: operator, value: parsed });
  }

  return (
    <div className="flex h-10 w-full items-stretch rounded-md border border-neutral-300 bg-background focus-within:border-blue-400 focus-within:ring-3 focus-within:ring-ring/20">
      <Menu>
        <MenuTrigger
          render={
            <Button
              type="button"
              variant="ghost"
              aria-label={`${label} operator`}
              className="h-auto w-16 shrink-0 justify-between rounded-none rounded-l-md border-r border-neutral-300 px-2 text-[14px]/6 text-neutral-1000"
            >
              {NUMERIC_SEARCH_OPERATOR_LABELS[operator]}
              <ChevronDown className="text-neutral-700" />
            </Button>
          }
        />
        <MenuContent align="start" className="min-w-16">
          {NUMERIC_SEARCH_OPERATORS.map((option) => (
            <MenuItem key={option} onClick={() => handleOperatorChange(option)}>
              {NUMERIC_SEARCH_OPERATOR_LABELS[option]}
            </MenuItem>
          ))}
        </MenuContent>
      </Menu>
      <input
        id={id}
        type="number"
        value={value?.value ?? ''}
        onChange={handleNumberChange}
        className="min-w-0 flex-1 bg-transparent px-3 text-[14px]/6 text-neutral-1000 outline-none placeholder:text-neutral-700"
      />
    </div>
  );
}

export { NumericSearchField };
