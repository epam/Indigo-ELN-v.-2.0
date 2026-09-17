import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { DICTIONARY_ITEMS, DICTIONARY_LIST, makeCurrentUser } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { routeTree } = await import('@/routeTree.gen');

const [FIRST] = DICTIONARY_LIST;

function respond(path: string) {
  if (path === '/api/eln/dictionaries') return Promise.resolve(DICTIONARY_LIST);
  if (path.endsWith('/full')) return Promise.resolve(DICTIONARY_ITEMS);
  if (path === '/api/eln/currentUser') return Promise.resolve(makeCurrentUser());
  return Promise.resolve([]);
}

async function renderDictionaries(initial = '/dictionaries') {
  const router = createRouter({ routeTree, history: createMemoryHistory({ initialEntries: [initial] }) });
  await router.load();
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router as never} />
    </QueryClientProvider>,
  );
  return router;
}

describe('dictionaries route', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /**
   * The exact URLs, which is the only place they are asserted now that the fetchers are
   * module-private. The `{dictionary}` segment is the **id**: a custom dictionary's `code` is
   * neither a `BuiltInDictionary` name nor a UUID, so `DictionaryService.refToID` would 404 on it.
   */
  it('loads the list, then the clicked dictionary by id', async () => {
    await renderDictionaries();

    await waitFor(() => expect(apiFetch.mock.calls.map(([path]) => path)).toContain('/api/eln/dictionaries'));
    // Nothing is selected, so no dictionary has been opened yet.
    expect(apiFetch.mock.calls.map(([path]) => String(path))).not.toContainEqual(expect.stringContaining('/full'));

    await userEvent.click(await screen.findByRole('button', { name: new RegExp(FIRST.name) }));

    await waitFor(() =>
      expect(apiFetch.mock.calls.map(([path]) => path)).toContain(`/api/eln/dictionaries/${FIRST.id}/full`),
    );
  });

  /** Opening a dictionary is a URL change, so the sheet is linkable and Back closes it. */
  it('puts the open dictionary in the search params', async () => {
    const router = await renderDictionaries();

    await userEvent.click(await screen.findByRole('button', { name: new RegExp(FIRST.name) }));

    await waitFor(() => expect(router.state.location.search).toMatchObject({ dictionary: FIRST.id }));
    await userEvent.click(screen.getByRole('button', { name: 'Close' }));
    // The param is dropped rather than set to an empty string, so a closed sheet leaves no trace.
    await waitFor(() => expect(router.state.location.search).not.toHaveProperty('dictionary'));
  });

  /** A link into a dictionary opens straight onto it, without a click to replay. */
  it('opens the sheet from a deep link', async () => {
    await renderDictionaries(`/dictionaries?dictionary=${FIRST.id}`);

    await waitFor(() =>
      expect(apiFetch.mock.calls.map(([path]) => path)).toContain(`/api/eln/dictionaries/${FIRST.id}/full`),
    );
    expect(await screen.findByText('About Dictionary')).toBeInTheDocument();
  });
});
