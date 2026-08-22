import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';
import { hashKey, QueryClient } from '@tanstack/react-query';
import type { PersistQueryClientProviderProps } from '@tanstack/react-query-persist-client';

import { ApiError } from '@/lib/api';
import { experimentKeys } from '@/lib/api/experiments';
import { userKeys } from '@/lib/api/user';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // Auth and not-found failures will not fix themselves on a retry.
      retry: (failureCount, error) => {
        if (error instanceof ApiError && error.status < 500) return false;
        return failureCount < 2;
      },
    },
  },
});

/**
 * Only the sidebar chrome is worth restoring from disk: it is on every screen, changes
 * rarely, and otherwise flashes skeletons on each load. Everything else — project pages,
 * keyword suggestions — stays in memory.
 */
const persistedHashes = new Set([userKeys.currentUser(), experimentKeys.marked()].map(hashKey));

let persister: ReturnType<typeof createSyncStoragePersister> | undefined;

/**
 * Scoped to the Cognito sub so a second user on the same browser starts from an empty
 * cache rather than restoring the previous user's name, permissions and starred list.
 */
export function persistOptions(userSub: string): PersistQueryClientProviderProps['persistOptions'] {
  persister = createSyncStoragePersister({
    storage: window.localStorage,
    key: `indigo-query-cache:${userSub}`,
  });

  return {
    persister,
    // Never expires on disk; each query's staleTime decides when to revalidate.
    maxAge: Infinity,
    dehydrateOptions: {
      shouldDehydrateQuery: (query) => query.state.status === 'success' && persistedHashes.has(query.queryHash),
    },
  };
}

/**
 * Signing out must not leave the previous user's name, permissions and starred list
 * readable on disk. Clearing first means a save still in the throttle window can only
 * write an empty cache, never user data.
 */
export async function clearPersistedCache() {
  queryClient.clear();
  await persister?.removeClient();
}
