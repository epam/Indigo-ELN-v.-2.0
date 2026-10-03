import { Select as SelectPrimitive } from '@base-ui/react/select';
import { ChevronDown } from 'lucide-react';
import { useRef, useState } from 'react';

import { INPUT_BOX, INPUT_BOX_FOCUS } from '@/components/ui/input';
import { cn } from '@/lib/utils';

/** Matches `ComboboxSize`: `md` is the form default, `sm` the 13px a dense table sets. */
type SelectSize = 'sm' | 'md';

const SIZE_TEXT: Record<SelectSize, string> = {
  sm: 'text-[13px]/5',
  md: 'text-[14px]/6',
};

/**
 * `sm` is 30px because that is what a text cell beside it comes to — a 20px line, `py-1` and the
 * 1px border of `EDITABLE_CELL_CLASS` — so a row of mixed cells is one height.
 */
const SIZE_BOX: Record<SelectSize, string> = {
  sm: 'h-7.5 pl-2',
  md: 'h-10 pl-3',
};

/**
 * `cell` is the select a dense table wants: at rest it is only its value, as plain as the text
 * and number cells beside it, and it becomes the box — border, background, chevron — when
 * hovered, focused or open. The trigger already *is* the value's text, so unlike `NumericCell`
 * there is no second form to swap in; the box is simply not drawn.
 */
type SelectVariant = 'box' | 'cell';

const CELL_TRIGGER = cn(
  'border-transparent bg-transparent enabled:hover:border-neutral-300',
  'focus-visible:border-blue-400 focus-visible:bg-background',
  'data-popup-open:border-blue-400 data-popup-open:bg-background',
  // A fixed value is plain text, not the box's 50% dimming — there is no box left to dim. What
  // marks an editable one is `CELL_VALUE_EDITABLE`'s underline.
  'disabled:cursor-default disabled:opacity-100',
);

/**
 * The dashed underline an editable number wears in the same tables — see `NumericCell`. In the
 * text's own colour, and gone once the box is drawn, which says the same thing.
 */
const CELL_VALUE_EDITABLE = cn(
  'underline decoration-dashed decoration-[0.5px] underline-offset-4',
  'group-focus-visible/select:no-underline group-data-popup-open/select:no-underline',
);

/**
 * Puts a `cell` back in its view state while it still holds focus — after Escape. The box is
 * drawn by `:focus-visible`, which only letting go of focus would clear, and letting go would
 * lose the user's place in the row. So the box's own classes are overruled instead, which takes
 * `!important`: a variant outranks a plain utility.
 */
const CELL_TRIGGER_DISMISSED = 'border-transparent! bg-transparent! ring-0!';
const CELL_VALUE_DISMISSED = 'underline!';
const CELL_ICON_DISMISSED = 'invisible!';

/** `invisible`, not `hidden`: the chevron keeps its width, so the value does not shift. */
const CELL_ICON = 'invisible group-focus-visible/select:visible group-data-popup-open/select:visible';

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
  /**
   * What the trigger reads before anything is chosen, greyed like an input's placeholder.
   *
   * A required field leaves `emptyLabel` off — see above — and would otherwise render a blank
   * trigger, since there is no selection to name and no clear-row to borrow a label from.
   * `emptyLabel` wins where both are set: that label is a real choice in the list, and the
   * trigger has to say which one is current.
   */
  placeholder?: string;
  /** Renders the current choice but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /** Whether the item list is still on its way. */
  loading?: boolean;
  /** Whether fetching the item list failed, so the popup is empty for a reason worth saying. */
  error?: boolean;
  /** Text size of the trigger and its popup. `sm` matches a dense table's 13px. */
  size?: SelectSize;
  /** `cell` hides the box until the select is hovered, focused or open — see `SelectVariant`. */
  variant?: SelectVariant;
  /**
   * Keeps the whole label on show rather than truncating it, so the label is the select's minimum
   * width — what a table column sized by its content needs. A `cell` always does.
   */
  fitContent?: boolean;
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
  placeholder,
  disabled = false,
  loading = false,
  error = false,
  size = 'md',
  variant = 'box',
  fitContent = variant === 'cell',
  className,
}: SelectProps<T>) {
  const cell = variant === 'cell';
  // Controlled only for `cell`, which opens on keyboard focus; `box` leaves it to Base UI.
  const [open, setOpen] = useState(false);
  /** Escape was pressed: show the value, not the box, until the cell is left or reopened. */
  const [dismissed, setDismissed] = useState(false);
  const popup = useRef<HTMLDivElement>(null);
  const status = loading ? 'Loading…' : error ? 'Could not load options' : null;
  const clearable = emptyLabel != null;
  const isEmpty = (item: T | Empty | null): item is Empty => item === EMPTY_ITEM;
  return (
    <SelectPrimitive.Root<T | Empty>
      value={clearable ? (value ?? EMPTY_ITEM) : value}
      onValueChange={(next) => onValueChange(next == null || isEmpty(next) ? null : next)}
      disabled={disabled}
      open={cell ? open : undefined}
      onOpenChange={(next, details) => {
        setOpen(next);
        // Opening again is editing again; Escape is the one way of closing that abandons it.
        setDismissed(cell && !next && details.reason === 'escape-key');
      }}
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
        /*
          Tabbing into a `cell` opens its list, as entering a numeric cell opens its units. Two
          things it must not answer to. A mouse click, which Base UI opens itself — hence
          `:focus-visible`. And focus coming *back* from the list after a pick or Escape, which
          would reopen it at once: that arrives from the popup, or from nowhere once the popup
          has unmounted, so only focus handed over by another control counts.
        */
        onFocus={
          cell
            ? (event) => {
                const from = event.relatedTarget;
                if (from == null || popup.current?.contains(from)) return;
                if (event.currentTarget.matches(':focus-visible')) setOpen(true);
              }
            : undefined
        }
        onBlur={cell ? () => setDismissed(false) : undefined}
        // Escape with the list already shut — after a pick, say — leaves the edit as well.
        onKeyDown={cell ? (event) => event.key === 'Escape' && !open && setDismissed(true) : undefined}
        className={cn(
          INPUT_BOX,
          INPUT_BOX_FOCUS,
          'group/select flex cursor-pointer items-center justify-between gap-1 pr-1',
          'text-left text-neutral-1000 outline-none',
          SIZE_BOX[size],
          SIZE_TEXT[size],
          // The trigger is a disabled element in its own right, so this stays a pseudo-variant
          // rather than `INPUT_DISABLED`. Same treatment as Input's and Combobox's, so a row of
          // mixed controls reads as one thing.
          'disabled:cursor-not-allowed disabled:opacity-50',
          cell && CELL_TRIGGER,
          dismissed && CELL_TRIGGER_DISMISSED,
          className,
        )}
      >
        {/*
          `Root` is given no `items` map, so `Value` receives the value itself rather than a
          resolved label — which is what we want, since `itemToLabel` is the caller's business.
        */}
        {/*
          Under `fitContent` the label is the column's minimum width, so a longer value widens
          the column instead of being clipped.
        */}
        <SelectPrimitive.Value
          className={cn(
            fitContent ? 'whitespace-nowrap' : 'truncate',
            cell && !disabled && CELL_VALUE_EDITABLE,
            dismissed && CELL_VALUE_DISMISSED,
            value == null && 'text-neutral-700',
          )}
        >
          {(item: T | Empty | null) =>
            item == null || isEmpty(item) ? (emptyLabel ?? placeholder ?? '') : itemToLabel(item)
          }
        </SelectPrimitive.Value>
        {/*
          Stands aside for the saving spinner exactly as the combobox's chevron does —
          `SavingOverlay` publishes `data-saving` on the group around this. `invisible` rather
          than `hidden`, so the control keeps its width and the spinner lands where the chevron was.
        */}
        <SelectPrimitive.Icon
          className={cn(
            'shrink-0 p-1 text-neutral-700 group-data-saving/saving:invisible',
            cell && CELL_ICON,
            dismissed && CELL_ICON_DISMISSED,
          )}
        >
          <ChevronDown className={size === 'sm' ? 'size-4' : 'size-5'} />
        </SelectPrimitive.Icon>
      </SelectPrimitive.Trigger>

      <SelectPrimitive.Portal>
        <SelectPrimitive.Positioner sideOffset={4} className="z-50" alignItemWithTrigger={false}>
          <SelectPrimitive.Popup
            ref={popup}
            className="max-h-60 min-w-(--anchor-width) overflow-y-auto rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none"
          >
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
                    'cursor-default rounded-2 px-3 py-2 text-neutral-700 outline-none data-highlighted:bg-blue-10',
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
                    'cursor-default rounded-2 px-3 py-2 outline-none data-highlighted:bg-blue-10',
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
