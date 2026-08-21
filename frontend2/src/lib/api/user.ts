import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { CurrentUserDTO } from '@/lib/types/user';

export const userKeys = {
  currentUser: () => ['currentUser'] as const,
};

export function fetchCurrentUser(): Promise<CurrentUserDTO> {
  return apiFetch<CurrentUserDTO>('currentUser');
}

export function useCurrentUser() {
  return useQuery({
    queryKey: userKeys.currentUser(),
    queryFn: fetchCurrentUser,
  });
}
