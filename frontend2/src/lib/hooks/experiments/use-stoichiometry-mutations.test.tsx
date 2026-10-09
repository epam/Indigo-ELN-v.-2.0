import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeExperimentDetails } from '@/mocks/fixtures';

import type { ModelMutation } from '@/lib/types/mutations.ts';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string, options?: { json?: unknown }) => apiFetch(path, options) };
});

const { useStoichiometryMutations } = await import('@/lib/hooks/experiments/use-stoichiometry-mutations');

const EXPERIMENT = makeExperimentDetails({ id: '22222222-2222-2222-2222-222222222222' });
const RESPONSE = { patch: {}, unresolvedInputs: {}, messages: [], debugMessages: [] };

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
  // A fresh client per test: the mutation scope lives on the MutationCache.
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

/** Holds every `/mutate` open until the test settles it, recording the anchors in the order they went out. */
function stubMutate() {
  const started: string[] = [];
  const pending: ReturnType<typeof deferred<unknown>>[] = [];
  apiFetch.mockImplementation((_path: string, options?: { json?: unknown }) => {
    started.push((options?.json as { anchor: string }).anchor);
    const request = deferred<unknown>();
    pending.push(request);
    return request.promise;
  });
  return { started, pending };
}

const register = (anchor: string): ModelMutation => ({ type: 'RegisterSample', anchor });

describe('useStoichiometryMutations', () => {
  // Braced: a returned function would be run by Vitest as a cleanup hook, calling the mock.
  beforeEach(() => {
    apiFetch.mockReset();
  });

  /**
   * Registering batch after batch down the column queues each write behind the last. TanStack runs
   * per-call `mutate` callbacks for the latest call only, so every cell must be cleared by its own
   * promise, or all but the last spin forever.
   */
  it('runs quick saves one at a time and clears each cell when its own write settles', async () => {
    const { started, pending } = stubMutate();
    const view = renderHook(() => useStoichiometryMutations(EXPERIMENT), { wrapper });

    act(() => {
      view.result.current.save('a:register', register('a'));
      view.result.current.save('b:register', register('b'));
      view.result.current.save('c:register', register('c'));
    });

    await waitFor(() => expect(started).toEqual(['a']));
    expect([...view.result.current.savingCells]).toEqual(['a:register', 'b:register', 'c:register']);

    await act(async () => pending[0].resolve(RESPONSE));
    await waitFor(() => expect(started).toEqual(['a', 'b']));
    expect([...view.result.current.savingCells]).toEqual(['b:register', 'c:register']);

    await act(async () => pending[1].resolve(RESPONSE));
    await waitFor(() => expect(started).toEqual(['a', 'b', 'c']));
    expect([...view.result.current.savingCells]).toEqual(['c:register']);

    await act(async () => pending[2].resolve(RESPONSE));
    await waitFor(() => expect(view.result.current.savingCells.size).toBe(0));
  });

  it('clears a failed write and still runs the next one', async () => {
    const { started, pending } = stubMutate();
    const view = renderHook(() => useStoichiometryMutations(EXPERIMENT), { wrapper });

    act(() => {
      view.result.current.save('a:register', register('a'));
      view.result.current.save('b:register', register('b'));
    });

    await waitFor(() => expect(started).toEqual(['a']));
    await act(async () => pending[0].reject(new Error('500')));
    await waitFor(() => expect(started).toEqual(['a', 'b']));
    expect([...view.result.current.savingCells]).toEqual(['b:register']);

    await act(async () => pending[1].resolve(RESPONSE));
    await waitFor(() => expect(view.result.current.savingCells.size).toBe(0));
  });
});
