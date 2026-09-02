import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { EXPERIMENTS, makeCurrentUser, makeNotebookDetails } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

const { routeTree } = await import('@/routeTree.gen');

const NOTEBOOK_ID = '77777777-7777-7777-7777-777777777777';
const NOTEBOOK = makeNotebookDetails({ id: NOTEBOOK_ID });

function respond(path: string) {
  if (path === `/api/eln/notebooks/${NOTEBOOK_ID}`) return Promise.resolve(NOTEBOOK);
  if (path.startsWith(`/api/eln/notebooks/${NOTEBOOK_ID}/experiments?`)) {
    return Promise.resolve({ pageNo: 0, pageSize: 10, totalItems: 9, totalPages: 1, items: EXPERIMENTS });
  }
  if (path === '/api/eln/currentUser') return Promise.resolve(makeCurrentUser());
  return Promise.resolve([]);
}

async function renderNotebook(initialPath = `/notebooks/${NOTEBOOK_ID}`) {
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

/** Every path the mocked apiFetch was asked for, in order. */
function requestedPaths(): string[] {
  return apiFetch.mock.calls.map(([path]) => path as string);
}

describe('notebook details page', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /**
   * The project comes off the notebook payload, not a second request — the URL is flat and
   * never carries it.
   */
  it('names the project and the notebook in the breadcrumb without fetching the project', async () => {
    await renderNotebook();

    expect(await screen.findByRole('link', { name: `Project: ${NOTEBOOK.projectName}` })).toHaveAttribute(
      'href',
      `/projects/${NOTEBOOK.projectId}`,
    );
    expect(screen.getByText(`Notebook: ${NOTEBOOK.name}`)).toBeInTheDocument();
    expect(requestedPaths()).not.toContain(`/api/eln/projects/${NOTEBOOK.projectId}`);
  });

  it('fills the header count from the notebook it loaded', async () => {
    await renderNotebook();

    expect(await screen.findByText(String(NOTEBOOK.experimentCount))).toBeInTheDocument();
  });

  it('shows the About and Team cards with the notebook it loaded', async () => {
    await renderNotebook();

    expect(await screen.findByRole('heading', { name: 'About Notebook' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Team' })).toBeInTheDocument();
    // The name, an attachment and a member — one field from each part of the tab.
    expect(screen.getByText(NOTEBOOK.name)).toBeInTheDocument();
    expect(screen.getByText('protocol.docx')).toBeInTheDocument();
    expect(screen.getByText('Sofia Rossi')).toBeInTheDocument();
  });

  /** The detail is fetched once and read by both the layout and the tab beneath it. */
  it('loads the notebook only once for the header and the tab', async () => {
    await renderNotebook();
    await screen.findByRole('heading', { name: 'About Notebook' });

    expect(requestedPaths().filter((path) => path === `/api/eln/notebooks/${NOTEBOOK_ID}`)).toHaveLength(1);
  });

  it('switches to the experiments tab and lists the notebook’s experiments', async () => {
    const router = await renderNotebook();
    await screen.findByRole('heading', { name: 'About Notebook' });

    await userEvent.click(screen.getByRole('link', { name: 'Experiments' }));

    await waitFor(() => expect(router.state.location.pathname).toBe(`/notebooks/${NOTEBOOK_ID}/experiments`));
    expect(await screen.findByLabelText('Search experiments')).toBeInTheDocument();
    expect(await screen.findByText(EXPERIMENTS[0].name)).toBeInTheDocument();
    // The exact URL, not just the prefix `respond` matches on: this is the only assertion left
    // on how the experiments query string is assembled, now that the fetch is module-private.
    expect(requestedPaths()).toContain(
      `/api/eln/notebooks/${NOTEBOOK_ID}/experiments?sort=LATEST&pageNo=0&pageSize=10`,
    );
    // The header survives the tab change rather than being re-rendered per tab.
    expect(screen.getByText(`Notebook: ${NOTEBOOK.name}`)).toBeInTheDocument();
  });

  /**
   * `status` is repeatable rather than comma-joined, which is the whole reason the experiments
   * list does not share `collectionQueryString` outright.
   */
  it('sends one status param per selected status, in display order', async () => {
    await renderNotebook(`/notebooks/${NOTEBOOK_ID}/experiments?status=SIGNING&status=OPEN`);

    await waitFor(() =>
      expect(requestedPaths()).toContain(
        `/api/eln/notebooks/${NOTEBOOK_ID}/experiments?sort=LATEST&pageNo=0&pageSize=10&status=SIGNING&status=OPEN`,
      ),
    );
  });

  it('reports a notebook it could not load instead of rendering an empty card', async () => {
    apiFetch.mockImplementation((path: string) =>
      path === `/api/eln/notebooks/${NOTEBOOK_ID}` ? Promise.reject(new Error('boom')) : respond(path),
    );

    await renderNotebook();

    expect(await screen.findByText('This notebook could not be loaded.')).toBeInTheDocument();
  });
});
