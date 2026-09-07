import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';
import { hashKey, QueryClient } from '@tanstack/react-query';
import type { PersistQueryClientOptions } from '@tanstack/react-query-persist-client';

import { ApiError } from '@/lib/api';
import { dictionaryKeys } from '@/lib/api/dictionaries';
import { experimentKeys } from '@/lib/api/experiments';
import { templateKeys } from '@/lib/api/templates';
import { userKeys } from '@/lib/api/user';
import { BUILT_IN_DICTIONARIES } from '@/lib/types/dictionaries.ts';

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
    hydrate: {
      queries: {
        // A restored entry nobody observes is built with the 5-minute default gcTime and
        // collected before it is ever read — and the save that follows then drops it from disk,
        // so the next load starts cold again. Only what `shouldDehydrateQuery` chose is ever
        // hydrated here, and all of it is meant to be kept. gcTime only ever grows
        // (Removable#updateGcTime takes the max), so a hook mounting later with a shorter one
        // cannot undo this.
        gcTime: Infinity,
      },
    },
  },
});

/**
 * What is worth restoring from disk: the sidebar chrome, which is on every screen, plus the
 * reference data that shapes the experiment screens — dictionaries, the template picker's list,
 * and the templates themselves. All of it changes only when an admin edits it, and all of it
 * otherwise flashes skeletons on each load. Everything else — project pages, keyword suggestions
 * — stays in memory.
 *
 * Nothing in the app mutates the dictionaries or the templates today: both admin screens are
 * placeholders (`src/routes/_auth/{dictionaries,templates}.tsx`). When they land, the
 * `invalidateQueries` they issue refetches and re-persists through the existing subscription,
 * with no change needed here.
 *
 * Every query listed here needs `gcTime: Infinity` on its hook. A collected query is gone from
 * the next dehydration, which takes it off disk as well.
 */
const persistedHashes = new Set(
  [
    userKeys.currentUser(),
    experimentKeys.marked(),
    templateKeys.list(),
    ...BUILT_IN_DICTIONARIES.map(dictionaryKeys.items),
  ].map(hashKey),
);

/**
 * Template details are keyed by id, so there is no fixed hash to list — every one is persisted,
 * and the set is bounded by how many templates exist. Read off the factory rather than written
 * out again: `projectDetails`, `notebookDetails` and `experimentDetails` are sibling roots that
 * must *not* match, so the literal has to be the one `templateKeys.detail` actually produces.
 */
const [TEMPLATE_DETAILS_ROOT] = templateKeys.detail('');

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
    shouldDehydrateQuery: (query) =>
      query.state.status === 'success' &&
      (persistedHashes.has(query.queryHash) || query.queryKey[0] === TEMPLATE_DETAILS_ROOT),
  },
} satisfies Omit<PersistQueryClientOptions, 'queryClient' | 'persister'>;
