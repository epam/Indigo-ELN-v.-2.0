import { Combobox } from '@/components/ui/combobox';
import { useDictionary } from '@/lib/api/dictionaries';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * A single-select over one built-in dictionary.
 *
 * The whole dictionary arrives in one request and the combobox filters it locally, suitable for small lists.
 */
function DictionaryCombobox({
  dictionary,
  value,
  onValueChange,
  id,
  disabled,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef | null;
  onValueChange: (value: DictionaryItemRef | null) => void;
  id: string;
  /** Renders the current pick but accepts no interaction — a reader who cannot edit. */
  disabled?: boolean;
}) {
  const { data, isPending, isError } = useDictionary(dictionary);

  return (
    <Combobox<DictionaryItemRef>
      id={id}
      value={value}
      onValueChange={onValueChange}
      items={data ?? []}
      itemToKey={(item) => item.id}
      itemToLabel={(item) => item.name}
      loading={isPending}
      // apiFetch has already toasted the failure; this says why the list is empty.
      error={isError}
      disabled={disabled}
    />
  );
}

export { DictionaryCombobox };
