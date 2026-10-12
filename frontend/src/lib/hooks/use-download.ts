import { useCallback, useState } from 'react';

/**
 * Runs a download and tracks whether one is in flight, so a call site is a click handler and a
 * disabled flag rather than its own `useState` and `try/finally`.
 *
 * It takes a thunk rather than a path, which keeps it ignorant of endpoints: the URL stays in
 * `src/lib/api/`, with the rest of them, and this works for any download added later.
 *
 * **The returned promise never rejects.** `apiFetch` has already raised the error toast by the
 * time `apiDownload` rethrows, so there is nothing left for a caller to report and every one of
 * them would write the same empty `catch`. That is what makes `void download(…)` safe.
 */
export function useDownload() {
  const [downloading, setDownloading] = useState(false);

  const download = useCallback(async (run: () => Promise<void>) => {
    setDownloading(true);
    try {
      await run();
    } catch {
      // Already reported by apiFetch's toast; nothing to add here.
    } finally {
      setDownloading(false);
    }
  }, []);

  return { download, downloading };
}
