import { useIsFetching, useQueryClient } from '@tanstack/react-query';
import { useCallback, useId } from 'react';

/**
 * Runs a download and tracks whether one is in flight, so a call site is a click handler and a
 * disabled flag rather than its own `useState` and `try/finally`.
 *
 * It takes a thunk rather than a path, which keeps it ignorant of endpoints: the URL stays in
 * `src/lib/api/`, with the rest of them, and this works for any download added later.
 *
 * A download is a read, so it runs as a query — one that caches nothing (`gcTime: 0`; the file is
 * saved by the thunk, and the query holds only `null`). The key is per hook instance: a second
 * click while one is in flight joins it rather than saving the file twice, and `downloading` is
 * that key's fetching state.
 *
 * **The returned promise never rejects**, which is what makes `void download(…)` safe.
 */
export function useDownload() {
  const queryClient = useQueryClient();
  const id = useId();
  const downloading = useIsFetching({ queryKey: ['download', id] }) > 0;

  const download = useCallback(
    async (run: () => Promise<void>) => {
      try {
        await queryClient.fetchQuery({
          queryKey: ['download', id],
          queryFn: async () => {
            await run();
            // A query may not resolve to `undefined`.
            return null;
          },
          staleTime: 0,
          gcTime: 0,
          retry: false,
        });
      } catch {
        // Silent: see `createCaches`.
      }
    },
    [queryClient, id],
  );

  return { download, downloading };
}
