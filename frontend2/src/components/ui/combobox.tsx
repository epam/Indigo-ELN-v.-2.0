import { Combobox as ComboboxPrimitive } from '@base-ui/react/combobox';
import { ChevronDown, Loader2, X } from 'lucide-react';
import type * as React from 'react';
import { useRef, useState } from 'react';

import { INPUT_ACTION, INPUT_BOX, INPUT_BOX_FOCUS_WITHIN, INPUT_DISABLED } from '@/components/ui/input';
import { cn } from '@/lib/utils';

/**
 * How far PageUp/PageDown jump through the suggestion list. Base UI declares
 * PAGE_UP/PAGE_DOWN but deliberately leaves them out of COMPOSITE_KEYS, so the list
 * ignores them; this component adds them by replaying that many arrow presses.
 */
const PAGE_STEP = 10;

/** Identity, for the common case where the items already are their own labels. */
/**
 * Text size of the control and of its popup. `md` is the form default; `sm` matches the 13px a
 * dense table sets, so a combobox in a cell does not read a size larger than the text beside it.
 */
export type ComboboxSize = 'sm' | 'md';

const SIZE_TEXT: Record<ComboboxSize, string> = {
  sm: 'text-[13px]/5',
  md: 'text-[14px]/6',
};

function identity(item: unknown): string {
  return String(item);
}

interface PopupContentProps<T> {
  items: T[];
  itemToKey: (item: T) => string;
  itemToLabel: (item: T) => string;
  /** How the list itself is doing, as opposed to what it contains. */
  statusContent: string | null;
  emptyContent: string | null;
  error: boolean;
  loading: boolean;
  size: ComboboxSize;
  listRef?: React.RefObject<HTMLDivElement | null>;
}

/**
 * The shared popup: status region, empty region, list. Both comboboxes render exactly
 * this, so the live-region rules below only have to be got right once.
 */
function PopupContent<T>({
  items,
  itemToKey,
  itemToLabel,
  statusContent,
  emptyContent,
  error,
  loading,
  size,
  listRef,
}: PopupContentProps<T>) {
  return (
    <ComboboxPrimitive.Portal>
      <ComboboxPrimitive.Positioner sideOffset={4} className="z-50">
        <ComboboxPrimitive.Popup className="max-h-60 w-(--anchor-width) overflow-y-auto rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none">
          {/*
            Status is Base UI's live region for the state of an asynchronously loaded
            list. Same rule as Empty below: keep it mounted, vary its children.
          */}
          <ComboboxPrimitive.Status>
            {statusContent && (
              <div
                className={cn(
                  'flex items-center gap-2 px-3 py-2',
                  SIZE_TEXT[size],
                  error ? 'text-red-200' : 'text-neutral-700',
                )}
              >
                {loading && <Loader2 className="size-4 shrink-0 animate-spin" />}
                {statusContent}
              </div>
            )}
          </ComboboxPrimitive.Status>
          {/*
            Base UI nulls this element's children once the list has items, and its docs
            require the element itself to stay mounted and visible — it is the popup's
            role="status" live region. So the padding sits on an inner node instead;
            on the element itself it would leave a blank strip above a populated list.
          */}
          <ComboboxPrimitive.Empty>
            {emptyContent && <div className={cn('px-3 py-2 text-neutral-700', SIZE_TEXT[size])}>{emptyContent}</div>}
          </ComboboxPrimitive.Empty>
          <ComboboxPrimitive.List ref={listRef}>
            {items.map((item) => (
              <ComboboxPrimitive.Item
                key={itemToKey(item)}
                value={item}
                className={cn(
                  'cursor-default rounded-2 px-3 py-2 outline-none data-highlighted:bg-blue-10',
                  SIZE_TEXT[size],
                )}
              >
                {itemToLabel(item)}
              </ComboboxPrimitive.Item>
            ))}
          </ComboboxPrimitive.List>
        </ComboboxPrimitive.Popup>
      </ComboboxPrimitive.Positioner>
    </ComboboxPrimitive.Portal>
  );
}

interface ComboboxProps<T> {
  /** The chosen item, or null. Every filter this backs is optional. */
  value: T | null;
  onValueChange: (value: T | null) => void;
  /** The full set to choose from — filtering is Base UI's, against `itemToLabel`. */
  items: T[];
  /** Identity of an item, for React keys. Defaults to the item stringified. */
  itemToKey?: (item: T) => string;
  /** What the item reads as, in the list and in the input. Defaults to the item stringified. */
  itemToLabel?: (item: T) => string;
  placeholder?: string;
  id?: string;
  /**
   * Names the control where no visible `<label>` does — a combobox sitting in a table cell,
   * whose column header is not associated with it. `MultiCombobox` has always taken one;
   * prefer `id` plus a `Field` wherever there is a label to point at.
   */
  'aria-label'?: string;
  /** Shown in the popup when nothing matches what was typed. */
  emptyMessage?: string;
  /** Whether the item list is still on its way. */
  loading?: boolean;
  /** Whether fetching the item list failed, so the popup is empty for a reason worth saying. */
  error?: boolean;
  /** Renders the current selection but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /** Text size of the control and its popup. `sm` matches a dense table's 13px. */
  size?: ComboboxSize;
}

/**
 * A single-select combobox: click to open the whole list, type to narrow it, ✕ to clear.
 *
 * Filtering is Base UI's own, i.e. client-side over `items` — the call sites here hold a
 * whole dictionary or a fixed enum in memory, so there is nothing to ask the server.
 * `MultiCombobox` below is the opposite case and turns that filter off.
 */
function Combobox<T>({
  value,
  onValueChange,
  items,
  itemToKey = identity,
  itemToLabel = identity,
  placeholder,
  id,
  'aria-label': ariaLabel,
  emptyMessage = 'No matches',
  loading = false,
  error = false,
  disabled = false,
  size = 'md',
}: ComboboxProps<T>) {
  const statusContent = loading ? 'Searching…' : error ? 'Could not load options' : null;
  // "No matches" is a claim about a finished search, so it survives neither a list still
  // loading nor one that failed to load at all.
  const emptyContent = loading || error ? null : emptyMessage;

  return (
    <ComboboxPrimitive.Root<T, false>
      disabled={disabled}
      items={items}
      value={value}
      onValueChange={onValueChange}
      itemToStringLabel={itemToLabel}
      // Object items are not referentially equal across refetches, so identity has to be
      // spelled out or a selected value stops matching its own row in the list.
      isItemEqualToValue={(a, b) => itemToKey(a) === itemToKey(b)}
    >
      <div
        className={cn(
          INPUT_BOX,
          INPUT_BOX_FOCUS_WITHIN,
          'flex h-10 items-center gap-1 pr-1 pl-3',
          // Matches Input's disabled treatment, so a form of mixed controls reads as one thing.
          disabled && INPUT_DISABLED,
        )}
      >
        <ComboboxPrimitive.Input
          id={id}
          aria-label={ariaLabel}
          placeholder={placeholder}
          className={cn(
            'min-w-0 flex-1 bg-transparent text-neutral-1000 outline-none placeholder:text-neutral-700',
            SIZE_TEXT[size],
          )}
        />
        {/* Base UI mounts this only while there is something to clear. */}
        <ComboboxPrimitive.Clear aria-label="Clear selection" className={INPUT_ACTION}>
          <X className="size-4" />
        </ComboboxPrimitive.Clear>
        {/*
          The chevron stays put while suggestions load. A spinner at the right edge of a field
          means **this field is being saved** — that is what `SavingOverlay` puts there — so
          borrowing the same spot for a lookup would say the wrong thing, and on a form that saves
          on blur the two would overlap outright. The wait is reported where it belongs: `Searching…`
          inside the popup, plus `aria-busy` here for anyone not looking at it.

          When a save *is* in flight the chevron gets out of the way instead: `SavingOverlay`
          publishes `data-saving` on the group around this. `invisible` rather than `hidden`, so
          the row keeps its width and the spinner lands exactly where the chevron was.
        */}
        <ComboboxPrimitive.Trigger aria-label="Show options" aria-busy={loading || undefined} className={INPUT_ACTION}>
          <ChevronDown className="size-5" />
        </ComboboxPrimitive.Trigger>
      </div>

      <PopupContent
        items={items}
        itemToKey={itemToKey}
        itemToLabel={itemToLabel}
        statusContent={statusContent}
        emptyContent={emptyContent}
        error={error}
        loading={loading}
        size={size}
      />
    </ComboboxPrimitive.Root>
  );
}

interface MultiComboboxProps<T> {
  /** The chosen chips. */
  value: T[];
  onValueChange: (value: T[]) => void;
  /** Suggestions to offer. Filtering is the caller's job — this list is shown verbatim. */
  items: T[];
  /** Identity of an item: React keys, de-duplication, and value matching. */
  itemToKey?: (item: T) => string;
  /** What the item reads as, on its chip and in the list. */
  itemToLabel?: (item: T) => string;
  inputValue: string;
  onInputValueChange: (inputValue: string) => void;
  placeholder?: string;
  id?: string;
  /** For call sites with no visible label — most pair this with `Field` instead. */
  'aria-label'?: string;
  /** Shown in the popup when there is nothing to offer. */
  emptyMessage?: string;
  /**
   * Whether text with no matching suggestion can be committed as a new chip. Off by
   * default: most multiselects must draw from a fixed server-side set. Only meaningful
   * for `T = string`, since a typed chip is the string itself.
   */
  allowCustomValues?: boolean;
  /**
   * Whether fresh suggestions are still on their way. Suppresses the empty-state message:
   * "no matches" is a claim about a search that has not finished.
   */
  loading?: boolean;
  /** Whether fetching suggestions failed, so the list is empty for a reason worth saying. */
  error?: boolean;
  /** Renders the chips but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /** Text size of the control, its chips and its popup. `sm` matches a dense table's 13px. */
  size?: ComboboxSize;
}

/**
 * A multi-select combobox rendering its selection as removable chips. Suggestion-only by
 * default; `allowCustomValues` additionally lets typed text be committed as a new chip.
 *
 * Base UI already handles most of the keyboard contract: Backspace on an empty input
 * deletes the last chip, ArrowLeft/ArrowRight walk the chips (Backspace/Delete removing
 * the highlighted one), ArrowUp/ArrowDown and Home/End move through the suggestions, and
 * Escape closes the list. Added here: PageUp/PageDown, and — when custom values are
 * allowed — Enter committing typed text while no suggestion is highlighted.
 */
function MultiCombobox<T = string>({
  value,
  onValueChange,
  items,
  itemToKey = identity,
  itemToLabel = identity,
  inputValue,
  onInputValueChange,
  placeholder,
  id,
  'aria-label': ariaLabel,
  emptyMessage = 'No matches',
  allowCustomValues = false,
  loading = false,
  error = false,
  disabled = false,
  size = 'md',
}: MultiComboboxProps<T>) {
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  // Enter is only ours to handle when Base UI has nothing highlighted to commit.
  const highlightedRef = useRef<T | undefined>(undefined);

  const [open, setOpen] = useState(false);

  const query = inputValue.trim();
  // Hint at Enter only once there is something to accept, and only when accepting it
  // would actually do something — addChip de-duplicates, so a value already held as a
  // chip would make the hint promise a no-op.
  const canAddQuery = allowCustomValues && query !== '' && !value.some((item) => itemToKey(item) === query);

  // How the list itself is doing, as opposed to what it contains.
  const statusContent = loading ? 'Searching…' : error ? 'Could not load suggestions' : null;

  // "No matches" is a claim about a finished search, so it survives neither a search still
  // running nor one that failed. The Enter hint does survive a failure: adding a custom
  // value never depended on the suggestions loading.
  const emptyContent =
    query === '' || loading ? null : canAddQuery ? `Press Enter to add “${query}”` : error ? null : emptyMessage;

  // Base UI would otherwise open an empty bordered box on focus or a trigger click.
  const hasContent = items.length > 0 || emptyContent !== null || statusContent !== null;

  function addChip(keyword: string) {
    const trimmed = keyword.trim();
    if (trimmed && !value.some((item) => itemToKey(item) === trimmed)) {
      // Reachable only under `allowCustomValues`, which is documented as T = string:
      // a chip typed by hand is the string itself.
      onValueChange([...value, trimmed as T]);
    }
    onInputValueChange('');
  }

  /**
   * Replays arrow presses on the input so Base UI's own list navigation moves the
   * highlight — it owns that state and exposes no way to set an index directly.
   * Dispatched natively because React's delegated listener picks bubbled events up.
   *
   * The count is clamped to the distance actually left in the list: Base UI's navigation
   * wraps around at the ends, so a fixed number of presses near the bottom would run past
   * it and back to the top. A page key should stop at the edge instead.
   */
  function stepPage(direction: 'ArrowUp' | 'ArrowDown') {
    const input = inputRef.current;
    const list = listRef.current;
    if (!input || !list) return;

    const options = Array.from(list.querySelectorAll('[role="option"]'));
    if (options.length === 0) return;

    const highlighted = options.findIndex((option) => option.hasAttribute('data-highlighted'));
    const down = direction === 'ArrowDown';
    // With nothing highlighted the first press lands on an end, so start one step outside
    // the list on the side the keys are travelling from.
    const from = highlighted === -1 ? (down ? -1 : options.length) : highlighted;
    const target = down ? Math.min(from + PAGE_STEP, options.length - 1) : Math.max(from - PAGE_STEP, 0);

    for (let i = 0; i < Math.abs(target - from); i += 1) {
      input.dispatchEvent(new KeyboardEvent('keydown', { key: direction, bubbles: true, cancelable: true }));
    }
  }

  function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'PageUp' || event.key === 'PageDown') {
      // Otherwise the dialog body scrolls out from under the open list.
      event.preventDefault();
      stepPage(event.key === 'PageUp' ? 'ArrowUp' : 'ArrowDown');
      return;
    }
    // A highlighted suggestion belongs to Base UI; a bare Enter is a custom value, which
    // only this branch handles — without the opt-in it does nothing at all.
    //
    // Enter and nothing else. Comma is the usual second commit key in a tag input, but not
    // here: chemistry names are full of them (N,N-dimethylformamide, 1,3-butadiene, 2,4-D)
    // and committing on comma would make those impossible to type. It is also what the
    // popup's own `Press Enter to add “…”` hint promises.
    if (allowCustomValues && event.key === 'Enter' && highlightedRef.current === undefined && inputValue.trim()) {
      event.preventDefault();
      addChip(inputValue);
    }
  }

  return (
    <ComboboxPrimitive.Root<T, true>
      multiple
      disabled={disabled}
      open={open && hasContent && !disabled}
      onOpenChange={setOpen}
      items={items}
      // The caller filters server-side; filtering again locally would hide fresh results.
      filter={null}
      value={value}
      onValueChange={onValueChange}
      itemToStringLabel={itemToLabel}
      // Object items are not referentially equal across refetches, so identity has to be
      // spelled out or a chip stops matching its own row in the list.
      isItemEqualToValue={(a, b) => itemToKey(a) === itemToKey(b)}
      inputValue={inputValue}
      onInputValueChange={onInputValueChange}
      onItemHighlighted={(item) => {
        highlightedRef.current = item;
      }}
    >
      <ComboboxPrimitive.Chips
        className={cn(
          INPUT_BOX,
          INPUT_BOX_FOCUS_WITHIN,
          // `min-h-10`, not the shell's usual `h-10`: the chips wrap, so the field grows.
          'flex min-h-10 flex-wrap items-center gap-2 px-2 py-1.5',
          disabled && INPUT_DISABLED,
        )}
      >
        {value.map((item) => (
          <ComboboxPrimitive.Chip
            key={itemToKey(item)}
            className="flex items-center gap-1 rounded-md bg-blue-10 py-0.5 pr-1 pl-2 text-[12px]/5 text-neutral-1000 outline-none data-highlighted:ring-3 data-highlighted:ring-ring/50"
          >
            {itemToLabel(item)}
            {/*
              Base UI already blocks this when the root is disabled (`ComboboxChipRemove` reads
              `comboboxDisabled || disabledProp`), and marks it `aria-disabled` + `data-disabled`
              rather than using the native attribute. Passing `disabled` explicitly says so at
              this level too; the styling is the part that was actually missing, since the button
              otherwise kept its pointer cursor and hover background and looked live.
            */}
            <ComboboxPrimitive.ChipRemove
              aria-label={`Remove ${itemToLabel(item)}`}
              disabled={disabled}
              className={cn(
                'rounded-full p-0.5 text-neutral-700',
                disabled ? 'pointer-events-none' : 'cursor-pointer hover:bg-blue-100 hover:text-neutral-1000',
              )}
            >
              <X className="size-3.5" />
            </ComboboxPrimitive.ChipRemove>
          </ComboboxPrimitive.Chip>
        ))}
        <ComboboxPrimitive.Input
          id={id}
          ref={inputRef}
          aria-label={ariaLabel}
          placeholder={value.length === 0 ? placeholder : undefined}
          onKeyDown={handleKeyDown}
          className={cn(
            'min-w-24 flex-1 bg-transparent text-neutral-1000 outline-none placeholder:text-neutral-700',
            SIZE_TEXT[size],
          )}
        />
        {/*
          The chevron stays put while suggestions load, and stands aside while the field is being
          saved — see `Combobox` above for both.
        */}
        <ComboboxPrimitive.Trigger
          aria-label="Show suggestions"
          aria-busy={loading || undefined}
          // The tighter inset is deliberate: `p-1` next to the chips crowds them.
          className={cn(INPUT_ACTION, 'p-0.5')}
        >
          <ChevronDown className="size-5" />
        </ComboboxPrimitive.Trigger>
      </ComboboxPrimitive.Chips>

      <PopupContent
        items={items}
        itemToKey={itemToKey}
        itemToLabel={itemToLabel}
        statusContent={statusContent}
        emptyContent={emptyContent}
        error={error}
        loading={loading}
        size={size}
        listRef={listRef}
      />
    </ComboboxPrimitive.Root>
  );
}

export { Combobox, MultiCombobox };
