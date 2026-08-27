import { Combobox } from '@/components/ui/combobox';
import { useDictionary } from '@/lib/api/dictionaries';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * A single-select over one built-in dictionary.
 *
 * The whole dictionary arrives in one request and the combobox filters it locally — these
 * lists are small, server-cached, and the `/suggest` endpoint caps at ten matches, which
 * would quietly hide options from a picker.
 */
function DictionaryCombobox({
  dictionary,
  value,
  onValueChange,
  id,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef | null;
  onValueChange: (value: DictionaryItemRef | null) => void;
  id: string;
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
    />
  );
}

export { DictionaryCombobox };
