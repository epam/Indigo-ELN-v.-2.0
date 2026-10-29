import { QueryClient } from '@tanstack/react-query';
import type { Query } from '@tanstack/react-query';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const notifyError = vi.fn();
vi.mock('@/lib/toast', () => ({ notifyError: (error: unknown) => notifyError(error) }));

import { ApiError } from '@/lib/api';
import { dictionaryKeys } from '@/lib/api/dictionaries';
import { experimentKeys } from '@/lib/api/experiments';
import { notebookKeys } from '@/lib/api/notebooks';
import { projectKeys } from '@/lib/api/projects';
import { templateKeys } from '@/lib/api/templates';
import { userKeys } from '@/lib/api/user';
import { createCaches, PERSIST_OPTIONS } from '@/lib/query-client';
import { BUILT_IN_DICTIONARIES } from '@/lib/types/dictionaries.ts';

const { shouldDehydrateQuery } = PERSIST_OPTIONS.dehydrateOptions;

/**
 * Widened, because every key factory returns its own tuple and `shouldDehydrateQuery` takes the
 * `Query<…, readonly unknown[]>` the cache is generic over.
 */
type Case = [label: string, queryKey: readonly unknown[]];

/** A real cache entry in the `success` state, which is the half of the rule these cases share. */
function loaded(queryKey: readonly unknown[]): Query {
  const client = new QueryClient();
  client.setQueryData(queryKey, { loaded: true });
  return client.getQueryCache().find({ queryKey, exact: true })!; // just written above
}

const PERSISTED: Case[] = [
  ['the current user', userKeys.currentUser()],
  ['the starred list', experimentKeys.marked()],
  ['the template list', templateKeys.list()],
  ['a template detail', templateKeys.detail('a1')],
  ['another template detail', templateKeys.detail('b2')],
  ...BUILT_IN_DICTIONARIES.map((d): Case => [`the ${d} dictionary`, dictionaryKeys.items(d)]),
];

/**
 * The three sibling `*Details` roots are why `templateDetails` is matched by its exact first
 * segment rather than by anything shaped like a detail key.
 */
const IN_MEMORY: Case[] = [
  ['a projects list', projectKeys.list({ search: '', sort: 'LATEST', createdByMe: false })],
  ['a project detail', projectKeys.detail('p1')],
  ['a notebook detail', notebookKeys.detail('n1')],
  ['an experiment detail', experimentKeys.detail('e1')],
  ['keyword suggestions', projectKeys.keywordSuggestions('acid')],
];

describe('PERSIST_OPTIONS.shouldDehydrateQuery', () => {
  it.each(PERSISTED)('persists %s', (_label, queryKey) => {
    expect(shouldDehydrateQuery(loaded(queryKey))).toBe(true);
  });

  it.each(IN_MEMORY)('leaves %s in memory', (_label, queryKey) => {
    expect(shouldDehydrateQuery(loaded(queryKey))).toBe(false);
  });

  it('skips a persisted key that has not loaded, so a placeholder is never written to disk', () => {
    const client = new QueryClient();
    const queryKey: readonly unknown[] = userKeys.currentUser();
    const query = client.getQueryCache().build(client, { queryKey });

    expect(query.state.status).toBe('pending');
    expect(shouldDehydrateQuery(query)).toBe(false);
  });
});

describe('createCaches', () => {
  beforeEach(() => notifyError.mockReset());

  function clientWithCaches() {
    return new QueryClient({
      ...createCaches(),
      defaultOptions: { queries: { retryDelay: 0 }, mutations: { retry: false } },
    });
  }

  /** Once, after the last retry: not a toast per attempt. */
  it('reports a failed query once its retries are spent', async () => {
    const error = new ApiError(500, null);
    const queryFn = vi.fn().mockRejectedValue(error);

    await expect(clientWithCaches().fetchQuery({ queryKey: ['failing'], queryFn, retry: 2 })).rejects.toBe(error);
    expect(queryFn).toHaveBeenCalledTimes(3);
    expect(notifyError).toHaveBeenCalledTimes(1);
    expect(notifyError).toHaveBeenCalledWith(error);
  });

  it('reports a failed request a mutation sent', async () => {
    const error = new ApiError(400, null);
    const client = clientWithCaches();
    const mutation = client.getMutationCache().build(client, {
      mutationFn: () => Promise.reject(error),
    });

    await expect(mutation.execute(undefined)).rejects.toBe(error);
    expect(notifyError).toHaveBeenCalledExactlyOnceWith(error);
  });

  /** The response arrived and the server saved; applying it is what failed, and still says so. */
  it('reports a mutation whose onSuccess threw', async () => {
    const error = new Error('patch expected to be an object');
    const client = clientWithCaches();
    const mutation = client.getMutationCache().build(client, {
      mutationFn: () => Promise.resolve('saved'),
      onSuccess: () => {
        throw error;
      },
    });

    await expect(mutation.execute(undefined)).rejects.toBe(error);
    expect(notifyError).toHaveBeenCalledExactlyOnceWith(error);
  });
});
