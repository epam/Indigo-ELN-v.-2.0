import {act, renderHook, waitFor} from '@testing-library/react';
import {describe, expect, it} from 'vitest';

import {useDownload} from './use-download';

/** A promise plus the handles to settle it, so the in-flight state is observable. */
function deferred() {
  let resolve!: () => void;
  let reject!: (error: Error) => void;
  const promise = new Promise<void>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe('useDownload', () => {
  it('starts idle', () => {
    const { result } = renderHook(() => useDownload());
    expect(result.current.downloading).toBe(false);
  });

  it('reports downloading for as long as the work is in flight', async () => {
    const { promise, resolve } = deferred();
    const { result } = renderHook(() => useDownload());

    act(() => void result.current.download(() => promise));
    await waitFor(() => expect(result.current.downloading).toBe(true));

    await act(async () => {
      resolve();
      await promise;
    });
    expect(result.current.downloading).toBe(false);
  });

  /** The error is already toasted by apiFetch, so callers can `void download(…)` safely. */
  it('clears the flag and resolves when the download fails', async () => {
    const { result } = renderHook(() => useDownload());

    await act(async () => {
      await expect(result.current.download(() => Promise.reject(new Error('boom')))).resolves.toBeUndefined();
    });

    expect(result.current.downloading).toBe(false);
  });

  it('keeps the same download callback across renders', () => {
    const { result, rerender } = renderHook(() => useDownload());
    const first = result.current.download;
    rerender();
    expect(result.current.download).toBe(first);
  });
});
