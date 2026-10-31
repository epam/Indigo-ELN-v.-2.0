import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeCurrentUser, makeTemplateDetails, TEMPLATES } from '@/mocks/fixtures';

vi.mock('@/lib/auth', () => ({
  auth: { getAccessToken: async () => 'token', requireLogin: async () => false, signOut: vi.fn() },
}));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string, init?: { method?: string; json?: unknown }) => apiFetch(path, init) };
});

const { routeTree } = await import('@/routeTree.gen');

// TEMPLATES[0] is the fixture named 'Default' — the starter Add Template copies.
const [STARTER, OTHER] = TEMPLATES;
const MANAGER = makeCurrentUser({ permissions: ['VIEW_PROJECTS', 'MANAGE_TEMPLATES'] });

let currentUser = MANAGER;

function respond(path: string, init?: { method?: string; json?: unknown }) {
  if (path === '/api/eln/currentUser') return Promise.resolve(currentUser);
  if (path === '/api/eln/templates' && init?.method === 'POST') {
    const body = init.json as { name: string };
    return Promise.resolve(makeTemplateDetails({ id: 'created', name: body.name }));
  }
  if (path.startsWith('/api/eln/templates?')) {
    return Promise.resolve({
      pageNo: 0,
      pageSize: 100,
      totalItems: 3,
      totalPages: 1,
      hasMore: false,
      items: TEMPLATES,
    });
  }
  const detail = path.match(/^\/api\/eln\/templates\/(.+)$/);
  if (detail) return Promise.resolve(makeTemplateDetails({ id: detail[1] }));
  return Promise.resolve([]);
}

async function renderTemplates(initial = '/templates') {
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

const paths = () => apiFetch.mock.calls.map(([path]) => String(path));

describe('templates route', () => {
  beforeEach(() => {
    currentUser = MANAGER;
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /**
   * The exact URLs, asserted here because the fetchers are module-private: the whole list in one
   * page (the backend's default page is 10), and the Default template Add Template starts from.
   */
  it('loads the whole list, and the starter template by id', async () => {
    await renderTemplates();

    await waitFor(() => expect(paths()).toContain('/api/eln/templates?pageSize=100'));
    await waitFor(() => expect(paths()).toContain(`/api/eln/templates/${STARTER.id}`));
  });

  /** Opening a template is a URL change, so the sheet is linkable and Back closes it. */
  it('puts the open template in the search params and loads its tabs', async () => {
    const router = await renderTemplates();

    await userEvent.click(await screen.findByRole('button', { name: new RegExp(OTHER.name) }));

    await waitFor(() => expect(router.state.location.search).toMatchObject({ template: OTHER.id }));
    await waitFor(() => expect(paths()).toContain(`/api/eln/templates/${OTHER.id}`));
    expect(await screen.findByText('About Template')).toBeInTheDocument();
  });

  /** Add Template posts to /templates — with the starter's structure under the typed name. */
  it('creates a template through POST /templates', async () => {
    await renderTemplates();

    await userEvent.click(await screen.findByRole('button', { name: 'Add Template' }));
    const name = await screen.findByLabelText(/Template Name/);
    await waitFor(() =>
      expect(screen.getByLabelText(/Tab 1 Name/)).toHaveValue(makeTemplateDetails().templateTabs[0].name),
    );
    await userEvent.type(name, 'Synthesis');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(apiFetch).toHaveBeenCalledWith('/api/eln/templates', {
        method: 'POST',
        json: { name: 'Synthesis', templateTabs: makeTemplateDetails().templateTabs },
      }),
    );
  });

  /** Without MANAGE_TEMPLATES the page is not shown at all. */
  it('turns away a user without MANAGE_TEMPLATES', async () => {
    currentUser = makeCurrentUser({ permissions: ['VIEW_PROJECTS'] });
    const router = await renderTemplates();

    await waitFor(() => expect(router.state.location.pathname).not.toBe('/templates'));
    expect(screen.queryByRole('heading', { name: 'Templates' })).not.toBeInTheDocument();
  });
});
