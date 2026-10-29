import { QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook, waitFor } from '@testing-library/react';
import { createElement } from 'react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';

import { useDownload } from './use-download';

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

function renderDownload(onError = vi.fn()) {
  const client = new QueryClient({ queryCache: new QueryCache({ onError }) });
  const wrapper = ({ children }: { children: ReactNode }) => createElement(QueryClientProvider, { client }, children);
  return { client, ...renderHook(() => useDownload(), { wrapper }) };
}

describe('useDownload', () => {
  it('starts idle', () => {
    const { result } = renderDownload();
    expect(result.current.downloading).toBe(false);
  });

  it('reports downloading for as long as the work is in flight', async () => {
    const { promise, resolve } = deferred();
    const { result } = renderDownload();

    act(() => void result.current.download(() => promise));
    await waitFor(() => expect(result.current.downloading).toBe(true));

    await act(async () => {
      resolve();
      await promise;
    });
    await waitFor(() => expect(result.current.downloading).toBe(false));
  });

  /** A second click while the first is in flight joins it rather than saving the file twice. */
  it('runs one download at a time per hook', async () => {
    const { promise, resolve } = deferred();
    const run = vi.fn(() => promise);
    const { result } = renderDownload();

    act(() => {
      void result.current.download(run);
      void result.current.download(run);
    });
    await act(async () => {
      resolve();
      await promise;
    });
    expect(run).toHaveBeenCalledTimes(1);
  });

  /** The query cache reports the failure, so callers can `void download(…)` safely. */
  it('reports a failure through the query cache and resolves', async () => {
    const onError = vi.fn();
    const error = new Error('boom');
    const { result } = renderDownload(onError);

    await act(async () => {
      await expect(result.current.download(() => Promise.reject(error))).resolves.toBeUndefined();
    });

    expect(onError).toHaveBeenCalledExactlyOnceWith(error, expect.anything());
    expect(result.current.downloading).toBe(false);
  });

  /** Nothing is kept: the file was saved by the thunk, and the query held only `null`. */
  it('caches nothing', async () => {
    const { client, result } = renderDownload();

    await act(() => result.current.download(() => Promise.resolve()));
    await waitFor(() => expect(client.getQueryCache().getAll()).toHaveLength(0));
  });

  it('keeps the same download callback across renders', () => {
    const { result, rerender } = renderDownload();
    const first = result.current.download;
    rerender();
    expect(result.current.download).toBe(first);
  });
});
