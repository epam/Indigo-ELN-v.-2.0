import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeCurrentUser, makeExperimentDetails, makeTemplateDetails, TEMPLATE_ID } from '@/mocks/fixtures';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string) => apiFetch(path) };
});

// The route's loader pulls in 21 MB of Ketcher for real otherwise. `renderStructure` has to
// resolve rather than be a bare `vi.fn()`: the reaction scheme in the stoichiometry panel awaits
// it on mount, and `undefined.then` would fail the render of the whole tab.
vi.mock('@/lib/ketcher', () => ({
  prewarmKetcher: vi.fn(),
  renderStructure: vi.fn().mockResolvedValue('data:image/svg+xml;base64,PHN2Zy8+'),
}));

const { routeTree } = await import('@/routeTree.gen');

const EXPERIMENT_ID = '22222222-2222-2222-2222-222222222222';
const EXPERIMENT = makeExperimentDetails({ id: EXPERIMENT_ID });
const TEMPLATE = makeTemplateDetails();

function respond(path: string) {
  if (path === `/api/eln/experiments/${EXPERIMENT_ID}`) return Promise.resolve(EXPERIMENT);
  if (path === `/api/eln/templates/${TEMPLATE_ID}`) return Promise.resolve(TEMPLATE);
  if (path === '/api/eln/currentUser') return Promise.resolve(makeCurrentUser());
  return Promise.resolve([]);
}

async function renderExperiment(initialPath = `/experiments/${EXPERIMENT_ID}`) {
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

describe('experiment details page', () => {
  beforeEach(() => {
    apiFetch.mockReset();
    apiFetch.mockImplementation(respond);
  });

  /** Both ancestors come off the experiment payload — the URL is flat and names neither. */
  it('names the project and the notebook in the breadcrumb without fetching either', async () => {
    await renderExperiment();

    expect(await screen.findByRole('link', { name: `Project: ${EXPERIMENT.projectName}` })).toHaveAttribute(
      'href',
      `/projects/${EXPERIMENT.projectId}`,
    );
    expect(screen.getByRole('link', { name: `Notebook: ${EXPERIMENT.notebookName}` })).toHaveAttribute(
      'href',
      `/notebooks/${EXPERIMENT.notebookId}`,
    );
    expect(screen.getByText(`Experiment: ${EXPERIMENT.name}`)).toBeInTheDocument();

    const paths = requestedPaths();
    expect(paths).not.toContain(`/api/eln/projects/${EXPERIMENT.projectId}`);
    expect(paths).not.toContain(`/api/eln/notebooks/${EXPERIMENT.notebookId}`);
  });

  /**
   * The exact URLs, not a prefix: this is the only assertion left on the two path templates,
   * now that both fetches are module-private.
   */
  it('loads the experiment once and the template it points at once', async () => {
    await renderExperiment();
    await screen.findByRole('link', { name: 'Experiment Info' });

    const paths = requestedPaths();
    expect(paths.filter((path) => path === `/api/eln/experiments/${EXPERIMENT_ID}`)).toHaveLength(1);
    expect(paths.filter((path) => path === `/api/eln/templates/${TEMPLATE_ID}`)).toHaveLength(1);
  });

  it('renders the template’s tabs and shows the first one', async () => {
    await renderExperiment();

    for (const tab of TEMPLATE.templateTabs) {
      expect(await screen.findByRole('link', { name: tab.name })).toBeInTheDocument();
    }
    expect(screen.getByRole('heading', { name: 'Experiment Details' })).toBeInTheDocument();
  });

  it('writes ?tab= when a tab is clicked and swaps the body', async () => {
    const router = await renderExperiment();
    await screen.findByRole('heading', { name: 'Experiment Details' });

    await userEvent.click(screen.getByRole('link', { name: 'Previous Versions' }));

    await waitFor(() => expect(router.state.location.search).toEqual({ tab: 'previous-versions' }));
    expect(await screen.findByRole('heading', { name: 'Version History' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Experiment Details' })).not.toBeInTheDocument();
  });

  it('restores the tab named by ?tab= on load', async () => {
    await renderExperiment(`/experiments/${EXPERIMENT_ID}?tab=summary`);

    expect(await screen.findByRole('heading', { name: 'Product Batch Summary' })).toBeInTheDocument();
  });

  /** The param is user-editable and outlives a template edit that renames a tab. */
  it('falls back to the first tab when ?tab= matches nothing', async () => {
    await renderExperiment(`/experiments/${EXPERIMENT_ID}?tab=nope`);

    expect(await screen.findByRole('heading', { name: 'Experiment Details' })).toBeInTheDocument();
  });

  /** The Attachments tab is where both newly-built components live. */
  it('shows the description editor and the attachment list on the Attachments tab', async () => {
    await renderExperiment(`/experiments/${EXPERIMENT_ID}?tab=attachments`);

    expect(await screen.findByRole('heading', { name: 'Attachments' })).toBeInTheDocument();
    expect(screen.getByText('protocol.docx')).toBeInTheDocument();
    // The description is an editor outright — the experiment screen has no edit mode to enter.
    expect(screen.getByRole('textbox')).toBeInTheDocument();
    // The card titles it, so the list must not say it a second time.
    expect(screen.getAllByRole('heading', { name: 'Attachments' })).toHaveLength(1);
  });

  it('reports an experiment it could not load instead of rendering an empty page', async () => {
    apiFetch.mockImplementation((path: string) =>
      path === `/api/eln/experiments/${EXPERIMENT_ID}` ? Promise.reject(new Error('boom')) : respond(path),
    );

    await renderExperiment();

    expect(await screen.findByText('This experiment could not be loaded.')).toBeInTheDocument();
  });
});
