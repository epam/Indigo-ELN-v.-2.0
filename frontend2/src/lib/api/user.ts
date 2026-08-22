import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { CurrentUser } from '@/lib/types/user';

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
  });
}
