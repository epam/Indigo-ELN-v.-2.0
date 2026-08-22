import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeCurrentUser, makeTotalCounts, PROJECTS } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { routeTree } = await import('@/routeTree.gen');

function respond(path: string) {
  if (path.startsWith('projects?')) {
    return Promise.resolve({ pageNo: 0, pageSize: 10, totalItems: 3, totalPages: 1, items: PROJECTS });
  }
  if (path === 'currentUser') return Promise.resolve(makeCurrentUser());
  if (path === 'total-counts') return Promise.resolve(makeTotalCounts());
  return Promise.resolve([]);
}

async function renderProjects() {
  const router = createRouter({ routeTree, history: createMemoryHistory({ initialEntries: ['/projects'] }) });
  await router.load();
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router as never} />
    </QueryClientProvider>,
  );
  await screen.findByLabelText('Search projects');
  return router;
}

describe('projects search box', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /**
   * The box is controlled straight from the URL search params now that the debounce lives
   * in the query rather than in a local draft. Router navigation is async, so this guards
   * the classic failure of a controlled input losing keystrokes it cannot keep up with.
   */
  it('keeps every character typed into it', async () => {
    const router = await renderProjects();
    const input = screen.getByLabelText('Search projects');

    await userEvent.type(input, 'kinase');

    expect(input).toHaveValue('kinase');
    await waitFor(() => expect(router.state.location.search).toMatchObject({ q: 'kinase' }));
  });

  /** Typing must not fire a request per keystroke — that is what the debounce is for. */
  it('asks the server only once the term stops changing', async () => {
    await renderProjects();
    const listCalls = () => apiFetch.mock.calls.filter(([path]) => String(path).startsWith('projects?'));
    const before = listCalls().length;

    await userEvent.type(screen.getByLabelText('Search projects'), 'kinase');
    expect(listCalls().length).toBe(before);

    await waitFor(() => expect(listCalls().some(([path]) => String(path).includes('search=kinase'))).toBe(true), {
      timeout: 2000,
    });
    expect(listCalls().length).toBe(before + 1);
  });
});
