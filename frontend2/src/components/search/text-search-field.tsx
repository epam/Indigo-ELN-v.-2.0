import { ChevronDown } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import type { TextSearch, TextSearchOperator } from '@/lib/types/search.ts';
import { TEXT_SEARCH_OPERATORS, TEXT_SEARCH_OPERATOR_LABELS } from '@/lib/types/search.ts';
import { cn } from '@/lib/utils';

/**
 * What should be emitted for an operator and the boxes as typed, or `null` when there is nothing
 * to search for.
 *
 * A blank box is not a filter — `TextSearchComponent` likewise emits null while its value is
 * blank — and `between` needs only one of its two bounds to be a range worth sending. The value
 * itself goes unmodified: leading space is not meaningful in a compound key, but neither is it
 * ours to silently drop from something the user typed.
 */
function toTextSearch(operator: TextSearchOperator, from: string, to: string): TextSearch | null {
  if (operator === 'between') {
    if (from.trim() === '' && to.trim() === '') return null;
    return { type: 'between', from, to };
  }
  return from.trim() === '' ? null : { type: operator, value: from };
}

/**
 * An operator picker and one or two text boxes, as one control: "contains aspirin".
 *
 * The sibling of `NumericSearchField`, down to the trap it documents: the pair is emitted as
 * `null` until a box holds something, so the operator has to survive locally in the meantime —
 * picked on an empty field it changes nothing upstream, but the button must still show what was
 * picked.
 *
 * Everything else is read straight off `value`, including the `between` bounds: a range with one
 * bound filled is a value this can emit, so there is no half-state needing a home here. The cost
 * is that switching an operator to or from `between` drops what was typed, since the two shapes
 * do not hold the same thing — the same trade `Select`-style operator swaps make everywhere.
 */
function TextSearchField({
  value,
  onValueChange,
  id,
  label,
  disabled,
}: {
  value: TextSearch | null;
  onValueChange: (value: TextSearch | null) => void;
  id: string;
  /** Names the operator button for screen readers, e.g. "Compound ID operator". */
  label: string;
  /** Renders what is set but accepts no interaction — what a PubChem catalog forces. */
  disabled?: boolean;
}) {
  const [pendingOperator, setPendingOperator] = useState<TextSearchOperator>('exact');
  // The committed value wins whenever there is one; otherwise the button shows the choice
  // waiting for something to be typed.
  const operator = value?.type ?? pendingOperator;

  const single = value != null && value.type !== 'between' ? value.value : '';
  const from = value?.type === 'between' ? value.from : '';
  const to = value?.type === 'between' ? value.to : '';

  const inputClass =
    'min-w-0 flex-1 bg-transparent px-3 text-[14px]/6 text-neutral-1000 outline-none disabled:cursor-not-allowed';

  return (
    <div
      className={cn(
        'flex h-10 w-full items-stretch rounded-md border border-neutral-300 bg-background',
        'focus-within:border-blue-400 focus-within:ring-3 focus-within:ring-ring/20',
        disabled && 'opacity-50',
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
              className="h-auto w-28 shrink-0 justify-between rounded-none rounded-l-md border-r border-neutral-300 px-2 text-[14px]/6 text-neutral-1000"
            >
              {TEXT_SEARCH_OPERATOR_LABELS[operator]}
              <ChevronDown className="text-neutral-700" />
            </Button>
          }
        />
        <MenuContent align="start" className="min-w-28">
          {TEXT_SEARCH_OPERATORS.map((option) => (
            <MenuItem
              key={option}
              onClick={() => {
                setPendingOperator(option);
                onValueChange(toTextSearch(option, option === 'between' ? from : single, to));
              }}
            >
              {TEXT_SEARCH_OPERATOR_LABELS[option]}
            </MenuItem>
          ))}
        </MenuContent>
      </Menu>

      {operator === 'between' ? (
        <>
          <input
            id={id}
            type="text"
            value={from}
            disabled={disabled}
            aria-label={`${label} from`}
            onChange={(event) => onValueChange(toTextSearch('between', event.target.value, to))}
            className={cn(inputClass, 'border-r border-neutral-300')}
          />
          <input
            type="text"
            value={to}
            disabled={disabled}
            aria-label={`${label} to`}
            onChange={(event) => onValueChange(toTextSearch('between', from, event.target.value))}
            className={inputClass}
          />
        </>
      ) : (
        <input
          id={id}
          type="text"
          value={single}
          disabled={disabled}
          onChange={(event) => onValueChange(toTextSearch(operator, event.target.value, ''))}
          className={inputClass}
        />
      )}
    </div>
  );
}

export { TextSearchField };
