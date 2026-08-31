import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeExperimentDetails } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { useEditExperiment, useExperimentAttachments, useToggleMark } = await import('@/lib/api/experiments');

const ID = '22222222-2222-2222-2222-222222222222';
const PATCH_PATH = `/api/eln/experiments/${ID}`;
const DELETE_PATH = `/api/eln/experiments/${ID}/attachments/a1`;
const MARK_PATH = `/api/eln/experiments/${ID}/mark`;

/** A promise the test settles by hand, so a request can be observed while still in flight. */
function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (error: Error) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  // Attached so an intentional rejection is never an unhandled one; the queue still sees it.
  promise.catch(() => {});
  return { promise, resolve, reject };
}

function wrapper({ children }: { children: ReactNode }) {
  // A fresh client per test: the mutation scope lives on the MutationCache, so a shared one
  // would carry a queue from one test into the next.
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

/** Records the order requests actually reach the network, and holds the PATCH open. */
function stubApi(held: Promise<unknown>) {
  const started: string[] = [];
  apiFetch.mockImplementation((path: string) => {
    started.push(path);
    return path === PATCH_PATH ? held : Promise.resolve(path === MARK_PATH ? true : undefined);
  });
  return started;
}

describe('experiment write queue', () => {
  beforeEach(() => apiFetch.mockReset());

  /**
   * Every write scoped by `experimentWrite` bumps the experiment's `revision` on the backend, so
   * two in flight race on it. Fields save on blur, which makes starting the second before the
   * first lands the normal case rather than an edge one.
   */
  it('runs a second write only once the first has settled', async () => {
    const patch = deferred<unknown>();
    const started = stubApi(patch.promise);

    const view = renderHook(() => ({ edit: useEditExperiment(ID), attachments: useExperimentAttachments(ID) }), {
      wrapper,
    });

    act(() => {
      view.result.current.edit.mutate({ description: '<p>New</p>' });
      view.result.current.attachments.remove.mutate('a1');
    });

    // The PATCH went out; the DELETE is queued behind it and has not been requested at all.
    await waitFor(() => expect(started).toEqual([PATCH_PATH]));
    // Both report pending, which is what keeps each of their controls frozen.
    expect(view.result.current.edit.isPending).toBe(true);
    expect(view.result.current.attachments.remove.isPending).toBe(true);

    await act(async () => {
      patch.resolve(makeExperimentDetails({ id: ID }));
      await patch.promise;
    });

    await waitFor(() => expect(started).toEqual([PATCH_PATH, DELETE_PATH]));
  });

  /** A failed write must hand off rather than wedge the queue — `runNext` runs from a `finally`. */
  it('runs the next write after one fails', async () => {
    const patch = deferred<unknown>();
    const started = stubApi(patch.promise);

    const view = renderHook(() => ({ edit: useEditExperiment(ID), attachments: useExperimentAttachments(ID) }), {
      wrapper,
    });

    act(() => {
      view.result.current.edit.mutate({ description: '<p>New</p>' });
      view.result.current.attachments.remove.mutate('a1');
    });
    await waitFor(() => expect(started).toEqual([PATCH_PATH]));

    await act(async () => {
      patch.reject(new Error('boom'));
      await patch.promise.catch(() => {});
    });

    await waitFor(() => expect(started).toEqual([PATCH_PATH, DELETE_PATH]));
  });

  /**
   * Starring is a per-user flag that never reaches `applyMutation`, so it is deliberately outside
   * the queue: it must not wait behind an unrelated save.
   */
  it('does not queue starring behind a write', async () => {
    const patch = deferred<unknown>();
    const started = stubApi(patch.promise);

    const view = renderHook(() => ({ edit: useEditExperiment(ID), mark: useToggleMark() }), { wrapper });

    act(() => {
      view.result.current.edit.mutate({ description: '<p>New</p>' });
      view.result.current.mark.mutate({ id: ID, marked: true });
    });

    await waitFor(() => expect(started).toEqual([PATCH_PATH, MARK_PATH]));
    // …and the write it overtook is still in flight.
    expect(view.result.current.edit.isPending).toBe(true);
  });
});
