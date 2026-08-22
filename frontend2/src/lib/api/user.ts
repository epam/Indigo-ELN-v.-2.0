import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { ApplicationPermission, CurrentUser } from '@/lib/types/user';

export const userKeys = {
  currentUser: () => ['currentUser'] as const,
};

export function fetchCurrentUser(): Promise<CurrentUser> {
  return apiFetch<CurrentUser>('currentUser');
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
