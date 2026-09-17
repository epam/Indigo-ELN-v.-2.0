import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { isBuiltInDictionary } from '@/lib/types/dictionaries.ts';

import type { UUID } from '@/lib/types/common.ts';
import type {
  BuiltInDictionary,
  Dictionary,
  DictionaryItem,
  DictionaryItemEditRequest,
  DictionaryItemRef,
} from '@/lib/types/dictionaries.ts';

/**
 * `items` is exported although no component reads it: `src/lib/query-client.ts` hashes it for
 * every built-in dictionary to decide what gets persisted to localStorage. Not a candidate for
 * going private.
 *
 * `full` sits under its own root rather than beneath `items`, so the admin table's cache and the
 * comboboxes' cache are invalidated independently — they hold different shapes of the same data
 * (`/full` keeps inactive words, `items` drops them).
 */
export const dictionaryKeys = {
  items: (dictionary: BuiltInDictionary) => ['dictionary', dictionary] as const,
  list: () => ['dictionaries'] as const,
  full: (id: UUID) => ['dictionaryItems', id] as const,
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

function fetchDictionaries(signal?: AbortSignal): Promise<Dictionary[]> {
  return apiFetch<Dictionary[]>('/api/eln/dictionaries', { signal });
}

/**
 * Every dictionary, for the admin list. The endpoint declares no query params at all — no
 * paging, no search, ordered by name — so there is nothing to pass and nothing to page.
 *
 * Not persisted: unlike the combobox lists this is read on one screen the user navigated to
 * deliberately, so it has no first-paint flash to spare.
 */
export function useDictionaries() {
  return useQuery({
    queryKey: dictionaryKeys.list(),
    queryFn: ({ signal }) => fetchDictionaries(signal),
  });
}

function fetchDictionaryItems(id: UUID, signal?: AbortSignal): Promise<DictionaryItem[]> {
  return apiFetch<DictionaryItem[]>(`/api/eln/dictionaries/${id}/full`, { signal });
}

/**
 * One dictionary's words, inactive ones included, in `ordinal` order — what the admin table
 * shows and `GET /dictionaries/{ref}` deliberately does not return.
 *
 * The sheet holding it is mounted before anything is selected (a sheet has to be mounted to
 * slide), so `undefined` is the ordinary first state rather than an error.
 */
export function useDictionaryItems(id: UUID | undefined) {
  return useQuery({
    queryKey: dictionaryKeys.full(id ?? ''),
    queryFn: ({ signal }) => fetchDictionaryItems(id!, signal), // enabled below guarantees it is set
    enabled: id !== undefined,
  });
}

/**
 * The three item writes differ only in method, path suffix and body — every one of them answers
 * with the dictionary's **whole** renumbered item list, and every one has the same two things to
 * do with it. So they are one factory rather than three near-copies of the same `onSuccess`.
 *
 * It takes the `Dictionary` rather than an id because both fields are needed: the `id` builds the
 * URL and keys the cache, while the `code` is what names the persisted combobox entry.
 */
function useItemMutation<V>(dictionary: Dictionary, request: (variables: V) => Promise<DictionaryItem[]>) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: request,
    onSuccess: (items) => {
      // The response *is* the new list, renumbered — so this replaces rather than refetches.
      queryClient.setQueryData(dictionaryKeys.full(dictionary.id), items);
      // A rename, a delete or an Active toggle all change what the comboboxes should offer, and
      // that list is persisted to localStorage. `query-client.ts` re-persists the refetch through
      // its existing subscription, so nothing there needs to know about this.
      if (isBuiltInDictionary(dictionary.code)) {
        void queryClient.invalidateQueries({ queryKey: dictionaryKeys.items(dictionary.code) });
      }
    },
  });
}

export function useAddDictionaryItem(dictionary: Dictionary) {
  return useItemMutation(dictionary, (name: string) =>
    apiFetch<DictionaryItem[]>(`/api/eln/dictionaries/${dictionary.id}`, {
      method: 'POST',
      json: { name },
    }),
  );
}

export function useUpdateDictionaryItem(dictionary: Dictionary) {
  return useItemMutation(dictionary, ({ itemId, edit }: { itemId: UUID; edit: DictionaryItemEditRequest }) =>
    apiFetch<DictionaryItem[]>(`/api/eln/dictionaries/${dictionary.id}/${itemId}`, {
      method: 'PATCH',
      json: edit,
    }),
  );
}

export function useRemoveDictionaryItem(dictionary: Dictionary) {
  return useItemMutation(dictionary, (itemId: UUID) =>
    apiFetch<DictionaryItem[]>(`/api/eln/dictionaries/${dictionary.id}/${itemId}`, { method: 'DELETE' }),
  );
}
