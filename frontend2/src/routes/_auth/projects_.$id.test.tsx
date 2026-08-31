import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {createMemoryHistory, createRouter, RouterProvider} from '@tanstack/react-router';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {beforeEach, describe, expect, it, vi} from 'vitest';

import {makeCurrentUser, makeProjectDetails, NOTEBOOKS} from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { routeTree } = await import('@/routeTree.gen');

const PROJECT_ID = '11111111-1111-1111-1111-111111111111';
const PROJECT = makeProjectDetails({ id: PROJECT_ID, name: 'Kinase Inhibitor Screening' });

function respond(path: string) {
  if (path === `/api/eln/projects/${PROJECT_ID}`) return Promise.resolve(PROJECT);
  if (path.startsWith(`/api/eln/projects/${PROJECT_ID}/notebooks?`)) {
    return Promise.resolve({ pageNo: 0, pageSize: 10, totalItems: 3, totalPages: 1, items: NOTEBOOKS });
  }
  if (path === '/api/eln/currentUser') return Promise.resolve(makeCurrentUser());
  return Promise.resolve([]);
}

async function renderProject(initialPath = `/projects/${PROJECT_ID}`) {
  const router = createRouter({ routeTree, history: createMemoryHistory({ initialEntries: [initialPath] }) });
  await router.load();
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router as never} />
    </QueryClientProvider>,
  );
  return router;
}

describe('project details page', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  it('names the project in the breadcrumb and fills the header counts', async () => {
    await renderProject();

    expect(await screen.findByText(`Project: ${PROJECT.name}`)).toBeInTheDocument();
    // notebookCount / experimentCount, which the stat tiles read off the detail.
    expect(await screen.findByText(String(PROJECT.notebookCount))).toBeInTheDocument();
    expect(screen.getByText(String(PROJECT.experimentCount))).toBeInTheDocument();
  });

  it('shows the About and Team cards with the project it loaded', async () => {
    await renderProject();

    expect(await screen.findByRole('heading', { name: 'About Project' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Team' })).toBeInTheDocument();
    // Keywords, an attachment and a member — one field from each part of the tab.
    expect(screen.getByText('kinase')).toBeInTheDocument();
    expect(screen.getByText('protocol.docx')).toBeInTheDocument();
    expect(screen.getByText('Mark Liu')).toBeInTheDocument();
  });

  /** The detail is fetched once and read by both the layout and the tab beneath it. */
  it('loads the project only once for the header and the tab', async () => {
    await renderProject();
    await screen.findByRole('heading', { name: 'About Project' });

    const detailCalls = apiFetch.mock.calls.filter(([path]) => path === `/api/eln/projects/${PROJECT_ID}`);
    expect(detailCalls).toHaveLength(1);
  });

  it('switches to the notebooks tab and lists the project’s notebooks', async () => {
    const router = await renderProject();
    await screen.findByRole('heading', { name: 'About Project' });

    await userEvent.click(screen.getByRole('link', { name: 'Notebooks' }));

    await waitFor(() => expect(router.state.location.pathname).toBe(`/projects/${PROJECT_ID}/notebooks`));
    expect(await screen.findByLabelText('Search notebooks')).toBeInTheDocument();
    expect(await screen.findByText(NOTEBOOKS[0].name)).toBeInTheDocument();
    // The exact URL, not just the prefix `respond` matches on: this is the only assertion left
    // on how the notebooks query string is assembled, now that the fetch is module-private.
    expect(apiFetch.mock.calls.map(([path]) => path)).toContain(
      `/api/eln/projects/${PROJECT_ID}/notebooks?sort=LATEST&pageNo=0&pageSize=10`,
    );
    // The header survives the tab change rather than being re-rendered per tab.
    expect(screen.getByText(`Project: ${PROJECT.name}`)).toBeInTheDocument();
  });

  it('reports a project it could not load instead of rendering an empty card', async () => {
    apiFetch.mockImplementation((path: string) =>
      path === `/api/eln/projects/${PROJECT_ID}` ? Promise.reject(new Error('boom')) : respond(path),
    );

    await renderProject();

    expect(await screen.findByText('This project could not be loaded.')).toBeInTheDocument();
  });
});
