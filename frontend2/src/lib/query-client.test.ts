import { QueryClient } from '@tanstack/react-query';
import type { Query } from '@tanstack/react-query';
import { describe, expect, it } from 'vitest';

import { dictionaryKeys } from '@/lib/api/dictionaries';
import { experimentKeys } from '@/lib/api/experiments';
import { notebookKeys } from '@/lib/api/notebooks';
import { projectKeys } from '@/lib/api/projects';
import { templateKeys } from '@/lib/api/templates';
import { userKeys } from '@/lib/api/user';
import { PERSIST_OPTIONS } from '@/lib/query-client';
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
