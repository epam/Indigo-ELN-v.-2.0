import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeCurrentUser, SIGNATURE_DOCUMENTS } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { routeTree } = await import('@/routeTree.gen');

const DOCUMENTS_PATH = '/api/signature/documents?';

function respond(path: string) {
  if (path.startsWith(DOCUMENTS_PATH)) {
    return Promise.resolve({
      pageNo: 0,
      pageSize: 10,
      totalItems: SIGNATURE_DOCUMENTS.length,
      totalPages: 1,
      items: SIGNATURE_DOCUMENTS,
    });
  }
  if (path === '/api/eln/currentUser') return Promise.resolve(makeCurrentUser());
  return Promise.resolve([]);
}

async function renderSignatures() {
  const router = createRouter({ routeTree, history: createMemoryHistory({ initialEntries: ['/signatures'] }) });
  await router.load();
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router as never} />
    </QueryClientProvider>,
  );
  await screen.findByLabelText('Search signatures');
  return router;
}

const listCalls = () => apiFetch.mock.calls.filter(([path]) => String(path).startsWith(DOCUMENTS_PATH));

describe('signatures list', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /** Same guard as the projects box: the input is controlled straight from the URL search params. */
  it('keeps every character typed into the search box', async () => {
    const router = await renderSignatures();
    const input = screen.getByLabelText('Search signatures');

    await userEvent.type(input, 'version');

    expect(input).toHaveValue('version');
    await waitFor(() => expect(router.state.location.search).toMatchObject({ q: 'version' }));
  });

  /**
   * The exact URL, not just the term. `signatureQueryParams` is module-private, so this is the
   * only assertion on how it is assembled — including that it sends `waitingMySignature` rather
   * than the ELN lists' `createdByMe`, and no `sortBy` at all.
   */
  it('asks the server only once the term stops changing', async () => {
    await renderSignatures();
    const before = listCalls().length;

    await userEvent.type(screen.getByLabelText('Search signatures'), 'version');
    expect(listCalls().length).toBe(before);

    await waitFor(
      () =>
        expect(listCalls().map(([path]) => path)).toContain(
          '/api/signature/documents?sort=LATEST&pageNo=0&pageSize=10&search=version',
        ),
      { timeout: 2000 },
    );
    expect(listCalls().length).toBe(before + 1);
  });

  /** The "My Entities" switch is the endpoint's `waitingMySignature`, and it takes effect at once. */
  it('sends waitingMySignature when My Entities is switched on', async () => {
    await renderSignatures();

    await userEvent.click(screen.getByRole('switch'));

    await waitFor(() =>
      expect(listCalls().map(([path]) => path)).toContain(
        '/api/signature/documents?sort=LATEST&pageNo=0&pageSize=10&waitingMySignature=true',
      ),
    );
  });
});
