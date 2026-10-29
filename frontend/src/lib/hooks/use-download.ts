import { useCallback, useState } from 'react';

import { notifyError } from '@/lib/toast';

/**
 * Runs a download and tracks whether one is in flight, so a call site is a click handler and a
 * disabled flag rather than its own `useState` and `try/finally`.
 *
 * It takes a thunk rather than a path, which keeps it ignorant of endpoints: the URL stays in
 * `src/lib/api/`, with the rest of them, and this works for any download added later.
 *
 * **The returned promise never rejects.** A download is neither a query nor a mutation, so no
 * cache reports its failure: it is toasted here, which leaves a caller nothing to report and spares
 * every one of them the same `catch`. That is what makes `void download(…)` safe.
 */
export function useDownload() {
  const [downloading, setDownloading] = useState(false);

  const download = useCallback(async (run: () => Promise<void>) => {
    setDownloading(true);
    try {
      await run();
    } catch (error) {
      notifyError(error);
    } finally {
      setDownloading(false);
    }
  }, []);

  return { download, downloading };
}
