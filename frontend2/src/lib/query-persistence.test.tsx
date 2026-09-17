import { dehydrate, QueryClient, useIsRestoring } from '@tanstack/react-query';
import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const fetchAuthSession = vi.fn();
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession }));

// Stands in for Amplify's event bus: `auth` is a protected channel, so `Hub.dispatch`
// from application code is dropped and the listeners have to be driven directly.
type AuthListener = (capsule: { payload: { event: string } }) => void;
const listeners = new Set<AuthListener>();
vi.mock('aws-amplify/utils', () => ({
  Hub: {
    listen: (_channel: string, callback: AuthListener) => {
      listeners.add(callback);
      return () => listeners.delete(callback);
    },
  },
}));

const { userKeys } = await import('@/lib/api/user');
const { PERSIST_OPTIONS, queryClient } = await import('@/lib/query-client');
const { QueryPersistenceProvider } = await import('@/lib/query-persistence');

const SUB = 'cognito-sub-1';
const CACHE_KEY = `indigo-query-cache:${SUB}`;
const STORED_USER = { username: 'alice', displayName: 'Alice Restored', permissions: [] };

/** What a previous session would have left on disk for `SUB`. */
function seedStoredCache() {
  const source = new QueryClient();
  source.setQueryData(userKeys.currentUser(), STORED_USER);
  window.localStorage.setItem(
    CACHE_KEY,
    JSON.stringify({
      buster: '',
      timestamp: Date.now(),
      clientState: dehydrate(source, PERSIST_OPTIONS.dehydrateOptions),
    }),
  );
}

/** Restoring spans several promise ticks: the session read, then the hydrate. */
async function flush() {
  await act(async () => {
    await Promise.resolve();
    await Promise.resolve();
  });
}

async function emit(event: string) {
  await act(async () => {
    listeners.forEach((callback) => callback({ payload: { event } }));
  });
  await flush();
}

/** Reports the gate the provider holds queries behind while a restore is in flight. */
function RestoringFlag() {
  return <span data-testid="restoring">{String(useIsRestoring())}</span>;
}

function renderProvider(initialUserSub?: string) {
  return render(
    <QueryPersistenceProvider initialUserSub={initialUserSub}>
      <RestoringFlag />
    </QueryPersistenceProvider>,
  );
}

const restoringFlag = () => screen.getByTestId('restoring').textContent;

const restoredUser = () => queryClient.getQueryData(userKeys.currentUser());

beforeEach(() => {
  fetchAuthSession.mockReset();
  window.localStorage.clear();
  queryClient.clear();
});

afterEach(() => {
  listeners.clear();
});

describe('QueryPersistenceProvider', () => {
  it('restores straight away for a page opened by a signed-in user', async () => {
    seedStoredCache();

    renderProvider(SUB);
    await flush();

    expect(restoredUser()).toEqual(STORED_USER);
  });

  it('engages persistence on a sign-in that happens after the app has loaded', async () => {
    // The regression: signing in is an SPA navigation, so a page that opened signed out
    // never reloads and used to spend the rest of its life unpersisted.
    seedStoredCache();
    fetchAuthSession.mockResolvedValue({ userSub: SUB });

    renderProvider();
    await flush();
    expect(restoredUser()).toBeUndefined();

    await emit('signedIn');

    expect(restoredUser()).toEqual(STORED_USER);
  });

  it('leaves the cache alone while nobody is signed in', async () => {
    seedStoredCache();

    renderProvider();
    await flush();

    expect(restoredUser()).toBeUndefined();
  });

  it('drops the cache in memory and on disk when the user signs out', async () => {
    seedStoredCache();

    renderProvider(SUB);
    await flush();
    expect(restoredUser()).toEqual(STORED_USER);

    await emit('signedOut');

    expect(restoredUser()).toBeUndefined();
    expect(window.localStorage.getItem(CACHE_KEY)).toBeNull();
  });

  it('holds queries only while a restore is actually in flight', async () => {
    seedStoredCache();

    // Signed out there is nothing to restore, so the gate must not be closed — leaving it
    // shut would hold every query in the app pending forever.
    renderProvider();
    expect(restoringFlag()).toBe('false');

    fetchAuthSession.mockResolvedValue({ userSub: SUB });
    await emit('signedIn');
    expect(restoringFlag()).toBe('false');

    // And it reopens on the way out, so a sign-out never strands the next screen.
    await emit('signedOut');
    expect(restoringFlag()).toBe('false');
  });

  it('closes the gate while a signed-in page restores', async () => {
    seedStoredCache();

    renderProvider(SUB);
    expect(restoringFlag()).toBe('true');

    await flush();
    expect(restoringFlag()).toBe('false');
  });

  it('restores the second user rather than the first when the session changes hands', async () => {
    seedStoredCache();
    renderProvider(SUB);
    await flush();

    await emit('signedOut');

    const otherSub = 'cognito-sub-2';
    fetchAuthSession.mockResolvedValue({ userSub: otherSub });
    await emit('signedIn');

    // Nothing was ever stored for the second user, so their cache starts empty rather
    // than inheriting the first user's name, permissions and starred list.
    expect(restoredUser()).toBeUndefined();
    expect(window.localStorage.getItem(`indigo-query-cache:${otherSub}`)).toBeNull();
  });
});
