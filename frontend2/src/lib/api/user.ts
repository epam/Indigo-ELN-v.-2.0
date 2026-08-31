import {useQuery} from '@tanstack/react-query';

import {apiFetch} from '@/lib/api';
import {useSettled} from '@/lib/hooks/use-settled';
import type {UserRef} from '@/lib/types/common.ts';
import type {ApplicationPermission, CurrentUser} from '@/lib/types/user';

/**
 * `currentUser` is exported although no component reads it: `src/lib/query-client.ts` needs the
 * hash to decide what gets persisted to localStorage. Not a candidate for going private.
 */
export const userKeys = {
  currentUser: () => ['currentUser'] as const,
  suggestions: (search: string) => ['userSuggestions', search] as const,
};

function fetchCurrentUser(): Promise<CurrentUser> {
  return apiFetch<CurrentUser>('/api/eln/currentUser');
}

export function useCurrentUser() {
  return useQuery({
    queryKey: userKeys.currentUser(),
    queryFn: fetchCurrentUser,
    // Permissions change rarely, and the entry is never evicted, so a refresh is
    // always a background refetch over existing data: `isPending` never flips back
    // to true and the nav does not reflow while it happens.
    staleTime: 15 * 60_000,
    gcTime: Infinity,
  });
}

/** `undefined` until currentUser resolves — callers decide what to show meanwhile. */
export function useHasPermission(permission: ApplicationPermission): boolean | undefined {
  const { data, isPending } = useCurrentUser();
  if (isPending) return undefined;
  return data?.permissions.includes(permission) ?? false;
}

/**
 * Case-insensitive prefix match across displayName/firstName/lastName/username, ordered by
 * display name and capped at 10 by the backend. Encoded because the term is interpolated
 * into a SQL LIKE, as with project keywords.
 */
function suggestUsers(search: string, signal?: AbortSignal): Promise<UserRef[]> {
  return apiFetch<UserRef[]>(`/api/eln/users/suggest?search=${encodeURIComponent(search)}`, { signal });
}

const SUGGEST_DEBOUNCE_MS = 300;

/**
 * Same shape as useKeywordSuggestions: the key tracks the term as typed and the debounce
 * gates `enabled`, so `isPending` is one honest "we don't know yet" spanning both the wait
 * and the request — which is what the combobox's `loading` prop wants. No `placeholderData`,
 * or holding the previous term's matches would flip the status to success and show answers
 * to a question no longer being asked.
 */
export function useUserSuggestions(search: string) {
  const settled = useSettled(search, SUGGEST_DEBOUNCE_MS);

  return useQuery({
    queryKey: userKeys.suggestions(search),
    // Consuming `signal` lets an abandoned lookup abort when the key moves on.
    queryFn: ({ signal }) => suggestUsers(search, signal),
    enabled: settled && search.length > 0,
  });
}
