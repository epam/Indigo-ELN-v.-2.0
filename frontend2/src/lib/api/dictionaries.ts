import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * Exported although no component reads it: `src/lib/query-client.ts` hashes `items()` for every
 * built-in dictionary to decide what gets persisted to localStorage. Not a candidate for going
 * private.
 */
export const dictionaryKeys = {
  items: (dictionary: BuiltInDictionary) => ['dictionary', dictionary] as const,
};

/**
 * The whole dictionary, active items only, ordered by `ordinal`. The path segment takes
 * the BuiltInDictionary name as well as a UUID (DictionaryService.refToID).
 */
function fetchDictionary(dictionary: BuiltInDictionary, signal?: AbortSignal): Promise<DictionaryItemRef[]> {
  return apiFetch<DictionaryItemRef[]>(`/api/eln/dictionaries/${dictionary}`, { signal });
}

/**
 * Deliberately the whole list rather than the `/suggest` endpoint: suggest is capped at
 * ten items and matches prefixes only, which would silently hide options from a picker.
 * The server answers these from an in-memory cache, so one fetch per dictionary is cheap
 * and the filtering happens in the combobox.
 */
export function useDictionary(dictionary: BuiltInDictionary) {
  return useQuery({
    queryKey: dictionaryKeys.items(dictionary),
    queryFn: ({ signal }) => fetchDictionary(dictionary, signal),
    // Dictionary contents change only when an admin edits them, and the list is small.
    staleTime: 15 * 60_000,
    // Persisted to localStorage, and a dictionary is only observed while a stoichiometry table
    // or a DictionaryCombobox is mounted — under the default gcTime the entry would be
    // collected five minutes after the user navigates away, and the next save would drop it
    // from disk with it.
    gcTime: Infinity,
  });
}
