import { Select as SelectPrimitive } from '@base-ui/react/select';
import { ChevronDown } from 'lucide-react';

import { cn } from '@/lib/utils';

/** Matches `ComboboxSize`: `md` is the form default, `sm` the 13px a dense table sets. */
type SelectSize = 'sm' | 'md';

const SIZE_TEXT: Record<SelectSize, string> = {
  sm: 'text-[13px]/5',
  md: 'text-[14px]/6',
};

/**
 * Stands in for "no selection" inside the list, and never leaves this file — callers see `null`.
 *
 * The obvious approach, an item whose value is literally `null`, does not work: Base UI already
 * uses `null` as its own marker for an unset select, so choosing such an item is read as choosing
 * nothing and `onValueChange` never fires. A sentinel of our own is a value like any other to
 * Base UI, and is translated back to `null` on the way out.
 */
const EMPTY_ITEM = Symbol('select-empty');
type Empty = typeof EMPTY_ITEM;

interface SelectProps<T> {
  /**
   * The chosen item, or `null`. A `null` is only reachable when `emptyLabel` is set — without it
   * the list offers no way to arrive at one, so a required field can treat it as impossible.
   */
  value: T | null;
  onValueChange: (value: T | null) => void;
  items: T[];
  /** Identity of an item, for React keys and for matching a value to its row. */
  itemToKey: (item: T) => string;
  /** What the item reads as, in the trigger and in the list. */
  itemToLabel: (item: T) => string;
  id?: string;
  /** Names the control where no visible `<label>` does — a select in a table cell. */
  'aria-label'?: string;
  /**
   * Adds a row that clears the selection, labelled with this. Set it for an optional field and
   * leave it off for a required one — the same distinction indigo-frontend drew with the
   * `required` flag on a column, which suppressed its blank `<mat-option>`.
   *
   * Clearing has to be a row in the list because there is no ✕ here; the list is the only way in
   * or out of a value.
   */
  emptyLabel?: string;
  /** Renders the current choice but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /** Whether the item list is still on its way. */
  loading?: boolean;
  /** Whether fetching the item list failed, so the popup is empty for a reason worth saying. */
  error?: boolean;
  /** Text size of the trigger and its popup. `sm` matches a dense table's 13px. */
  size?: SelectSize;
  className?: string;
}

/**
 * A closed list of choices: click to open, pick one, done.
 *
 * **Deliberately not `Combobox`**, which it deliberately resembles. A combobox offers three
 * things a fixed enum does not want — a text input to filter with, a ✕ to clear the selection,
 * and an empty state for "nothing matched what you typed". Offering them where the option list
 * is four items long and the field is `@NotNull` invites a user to type into something that
 * cannot take free text, and to clear a value the backend will not accept as absent.
 *
 * So the trigger is a button rather than an input. An optional field sets `emptyLabel`, which
 * puts "clear this" in the list where every other choice already is; a required one leaves it off
 * and can then treat `null` as unreachable.
 *
 * Reach for `Combobox` instead when the list is long enough to want filtering.
 */
function Select<T>({
  value,
  onValueChange,
  items,
  itemToKey,
  itemToLabel,
  id,
  'aria-label': ariaLabel,
  emptyLabel,
  disabled = false,
  loading = false,
  error = false,
  size = 'md',
  className,
}: SelectProps<T>) {
  const status = loading ? 'Loading…' : error ? 'Could not load options' : null;
  const clearable = emptyLabel != null;
  const isEmpty = (item: T | Empty | null): item is Empty => item === EMPTY_ITEM;
  return (
    <SelectPrimitive.Root<T | Empty>
      value={clearable ? (value ?? EMPTY_ITEM) : value}
      onValueChange={(next) => onValueChange(next == null || isEmpty(next) ? null : next)}
      disabled={disabled}
      // Object items are not referentially equal across refetches, so identity has to be
      // spelled out or a selected value stops matching its own row in the list. Null is only
      // ever equal to itself — `itemToKey` is the caller's and has no reason to expect one.
      isItemEqualToValue={(a, b) =>
        a == null || b == null || isEmpty(a) || isEmpty(b) ? a === b : itemToKey(a) === itemToKey(b)
      }
    >
      <SelectPrimitive.Trigger
        id={id}
        aria-label={ariaLabel}
        className={cn(
          'flex h-10 w-full cursor-pointer items-center justify-between gap-1 rounded-md border border-neutral-300 bg-background pr-1 pl-3',
          'text-left text-neutral-1000 outline-none',
          SIZE_TEXT[size],
          'focus-visible:border-blue-400 focus-visible:ring-3 focus-visible:ring-ring/20',
          // Matches Input's and Combobox's disabled treatment, so a row of mixed controls
          // reads as one thing.
          'disabled:cursor-not-allowed disabled:opacity-50',
          className,
        )}
      >
        {/*
          `Root` is given no `items` map, so `Value` receives the value itself rather than a
          resolved label — which is what we want, since `itemToLabel` is the caller's business.
        */}
        <SelectPrimitive.Value className="truncate">
          {(item: T | Empty | null) => (item == null || isEmpty(item) ? (emptyLabel ?? '') : itemToLabel(item))}
        </SelectPrimitive.Value>
        {/*
          Stands aside for the saving spinner exactly as the combobox's chevron does —
          `SavingOverlay` publishes `data-saving` on the group around this. `invisible` rather
          than `hidden`, so the control keeps its width and the spinner lands where the chevron was.
        */}
        <SelectPrimitive.Icon className="shrink-0 p-1 text-neutral-700 group-data-[saving]/saving:invisible">
          <ChevronDown className="size-5" />
        </SelectPrimitive.Icon>
      </SelectPrimitive.Trigger>

      <SelectPrimitive.Portal>
        <SelectPrimitive.Positioner sideOffset={4} className="z-50" alignItemWithTrigger={false}>
          <SelectPrimitive.Popup className="max-h-[240px] min-w-[var(--anchor-width)] overflow-y-auto rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none">
            <SelectPrimitive.List>
              {status && (
                <div className={cn('px-3 py-2', SIZE_TEXT[size], error ? 'text-red-200' : 'text-neutral-700')}>
                  {status}
                </div>
              )}
              {clearable && (
                <SelectPrimitive.Item
                  value={EMPTY_ITEM}
                  className={cn(
                    'cursor-default rounded-2 px-3 py-2 text-neutral-700 outline-none data-[highlighted]:bg-blue-10',
                    SIZE_TEXT[size],
                  )}
                >
                  <SelectPrimitive.ItemText>{emptyLabel}</SelectPrimitive.ItemText>
                </SelectPrimitive.Item>
              )}
              {items.map((item) => (
                <SelectPrimitive.Item
                  key={itemToKey(item)}
                  value={item}
                  className={cn(
                    'cursor-default rounded-2 px-3 py-2 outline-none data-[highlighted]:bg-blue-10',
                    SIZE_TEXT[size],
                  )}
                >
                  <SelectPrimitive.ItemText>{itemToLabel(item)}</SelectPrimitive.ItemText>
                </SelectPrimitive.Item>
              ))}
            </SelectPrimitive.List>
          </SelectPrimitive.Popup>
        </SelectPrimitive.Positioner>
      </SelectPrimitive.Portal>
    </SelectPrimitive.Root>
  );
}

export { Select };
