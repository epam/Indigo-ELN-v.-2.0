import { IsRestoringProvider, QueryClientProvider } from '@tanstack/react-query';
import type { Persister } from '@tanstack/react-query-persist-client';
import { persistQueryClientRestore, persistQueryClientSubscribe } from '@tanstack/react-query-persist-client';
import { fetchAuthSession } from 'aws-amplify/auth';
import { Hub } from 'aws-amplify/utils';
import type { ReactNode } from 'react';
import { useEffect, useRef, useState } from 'react';

import { createPersister, PERSIST_OPTIONS, queryClient } from '@/lib/query-client';

/**
 * Provides the query client, and keeps the persisted cache attached to whoever is
 * actually signed in.
 *
 * `PersistQueryClientProvider` cannot do this: it reads `persistOptions` from a ref, keys
 * its effect on the client alone, and latches `didRestore`, so handing it a new storage
 * key never re-runs a restore. Since signing in is an SPA navigation — the app is not
 * reloaded — a page that opened signed out would otherwise spend the rest of its life
 * with persistence switched off.
 */
function QueryPersistenceProvider({ initialUserSub, children }: { initialUserSub?: string; children: ReactNode }) {
  const [userSub, setUserSub] = useState(initialUserSub);
  // The sub whose restore has finished, compared against the current one rather than kept
  // as its own boolean: there is then no render where a newly signed-in user still looks
  // restored, and signing out needs nothing reset — no sub is trivially not restoring.
  const [restoredSub, setRestoredSub] = useState<string | undefined>(undefined);
  // The persister this session is saving through, so that signing out can delete the
  // right key *after* the subscription feeding it has been torn down.
  const persisterRef = useRef<Persister | undefined>(undefined);

  const isRestoring = userSub !== undefined && restoredSub !== userSub;

  useEffect(() => {
    return Hub.listen('auth', ({ payload }) => {
      if (payload.event === 'signedOut') {
        // Cleared here, while the save subscription is still live, so that a write already
        // scheduled inside the persister's throttle window replaces its snapshot with this
        // empty one. Clearing only after the unsubscribe below would leave that pending
        // write holding the signed-out user's data, to land after the key was removed.
        queryClient.clear();
        setRestoredSub(undefined);
        setUserSub(undefined);
        return;
      }
      if (payload.event !== 'signedIn') return;
      // Read the sub the way the bootstrap in main.tsx does, so the storage key has a
      // single definition rather than one per event shape.
      void fetchAuthSession()
        .then((session) => setUserSub(session.userSub))
        .catch(() => setUserSub(undefined));
    });
  }, []);

  useEffect(() => {
    if (userSub === undefined) {
      // Nobody is signed in, so nothing cached may remain readable — the invariant holds
      // whether this is a sign-out or a page that simply opened signed out. The cleanup
      // below has already unsubscribed, so no later save can undo the removal.
      const previous = persisterRef.current;
      persisterRef.current = undefined;
      queryClient.clear();
      void previous?.removeClient();
      return;
    }

    const persister = createPersister(userSub);
    persisterRef.current = persister;
    const options = { ...PERSIST_OPTIONS, queryClient, persister };

    let cancelled = false;
    let unsubscribe: (() => void) | undefined;

    // Saving only starts once the restore has finished, or the empty cache it starts from
    // would be written straight over the data being read back.
    void persistQueryClientRestore(options)
      // A cache that fails to restore is discarded by restore itself; carry on unpersisted
      // rather than leaving every query in the app held behind `isRestoring`.
      .catch(() => {})
      .finally(() => {
        if (cancelled) return;
        setRestoredSub(userSub);
        unsubscribe = persistQueryClientSubscribe(options);
      });

    return () => {
      cancelled = true;
      unsubscribe?.();
    };
  }, [userSub]);

  return (
    <QueryClientProvider client={queryClient}>
      <IsRestoringProvider value={isRestoring}>{children}</IsRestoringProvider>
    </QueryClientProvider>
  );
}

export { QueryPersistenceProvider };
