import { useState } from 'react';

import { Select } from '@/components/ui/select';
import { useDictionary } from '@/lib/api/dictionaries';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * A single-select over one built-in dictionary.
 *
 * The whole dictionary arrives in one request. A dictionary is a closed list, so there is nothing
 * to type into: the control is a `Select`, where a letter key jumps to the item it starts.
 */
function DictionaryCombobox({
  dictionary,
  value,
  onValueChange,
  id,
  'aria-label': ariaLabel,
  placeholder,
  disabled,
  clearable = true,
  size,
  variant,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef | null;
  onValueChange: (value: DictionaryItemRef | null) => void;
  id?: string;
  /** See `Select`. */
  'aria-label'?: string;
  /** See `Select` — shown only where `clearable` is off, since the blank row's label wins. */
  placeholder?: string;
  /** Renders the current pick but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /**
   * Whether the list opens with a blank row that clears the pick. Off for a required field, which
   * can be changed but not emptied — `onValueChange` is never given null then.
   */
  clearable?: boolean;
  /** See `Select`. */
  size?: 'sm' | 'md';
  /** See `Select`. */
  variant?: 'box' | 'cell';
}) {
  const { data, isPending, isError } = useDictionary(dictionary);

  return (
    <Select<DictionaryItemRef>
      id={id}
      aria-label={ariaLabel}
      placeholder={placeholder}
      value={value}
      onValueChange={onValueChange}
      items={data ?? []}
      itemToKey={(item) => item.id}
      itemToLabel={(item) => item.name}
      // A no-break space, so the blank row is still a line tall.
      emptyLabel={clearable ? ' ' : undefined}
      loading={isPending}
      // apiFetch has already toasted the failure; this says why the list is empty.
      error={isError}
      disabled={disabled}
      size={size}
      variant={variant}
    />
  );
}

/**
 * Several items from one built-in dictionary, shown as chips. Like `DictionaryCombobox` there is
 * nothing to type: the list ticks what is chosen, and picking a ticked row again removes it.
 *
 * **The picks are a draft until the list is left, and then one `onValueChange`.** Every use of
 * this saves as it changes, the list stays open across ticks and nothing is optimistic — so
 * reporting each tick computed the second from a value the first had not yet changed, and the
 * server ended with only the second. Escape abandons the draft.
 */
function MultiDictionaryCombobox({
  dictionary,
  value,
  onValueChange,
  pending = false,
  id,
  'aria-label': ariaLabel,
  placeholder,
  disabled,
  size,
  variant,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef[];
  /** Called once per visit to the list, as it closes, and only if the picks changed. */
  onValueChange: (value: DictionaryItemRef[]) => void;
  /** Whether what `onValueChange` last reported is still being saved; it stays on show meanwhile. */
  pending?: boolean;
  id?: string;
  /** See `Select`. */
  'aria-label'?: string;
  /** See `Select`. */
  placeholder?: string;
  /** Renders the current picks but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
  /** See `Select`. */
  size?: 'sm' | 'md';
  /** See `Select`. */
  variant?: 'box' | 'cell';
}) {
  const { data, isPending, isError } = useDictionary(dictionary);
  /** What has been ticked on this visit to the list; null until something is. */
  const [draft, setDraft] = useState<DictionaryItemRef[] | null>(null);
  /** What the last visit reported, read only while that save is in flight — see `NumericCell`'s. */
  const [sent, setSent] = useState(value);
  const saved = pending ? sent : value;

  return (
    <Select<DictionaryItemRef>
      multiple
      id={id}
      aria-label={ariaLabel}
      placeholder={placeholder}
      value={draft ?? saved}
      onValueChange={setDraft}
      onOpenChange={(open, dismissed) => {
        if (open) return;
        setDraft(null);
        if (draft == null || dismissed) return;
        // Order is the order of ticking, which is not a change.
        const ids = (items: DictionaryItemRef[]) => String(items.map((item) => item.id).sort());
        if (ids(draft) === ids(saved)) return;
        setSent(draft);
        onValueChange(draft);
      }}
      items={data ?? []}
      itemToKey={(item) => item.id}
      itemToLabel={(item) => item.name}
      loading={isPending}
      // apiFetch has already toasted the failure; this says why the list is empty.
      error={isError}
      disabled={disabled}
      size={size}
      variant={variant}
    />
  );
}

export { DictionaryCombobox, MultiDictionaryCombobox };
