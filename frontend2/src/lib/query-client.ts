import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';
import { hashKey, QueryClient } from '@tanstack/react-query';
import type { PersistQueryClientOptions } from '@tanstack/react-query-persist-client';

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

/**
 * A persister for one signed-in user. Scoped to the Cognito sub so a second user on the
 * same browser starts from an empty cache rather than restoring the previous user's name,
 * permissions and starred list.
 *
 * Built per user rather than once per page, because a session can change hands without a
 * reload: signing in is an SPA navigation, so the key is only known after the fact — see
 * `QueryPersistenceProvider`.
 */
export function createPersister(userSub: string) {
  return createSyncStoragePersister({
    storage: window.localStorage,
    key: `indigo-query-cache:${userSub}`,
  });
}

/**
 * Shared by the restore and the subscription, which have to agree on what is stored: a
 * save that dehydrated more than the restore expects would grow the cache silently.
 */
export const PERSIST_OPTIONS = {
  // Never expires on disk; each query's staleTime decides when to revalidate.
  maxAge: Infinity,
  dehydrateOptions: {
    shouldDehydrateQuery: (query) => query.state.status === 'success' && persistedHashes.has(query.queryHash),
  },
} satisfies Omit<PersistQueryClientOptions, 'queryClient' | 'persister'>;
