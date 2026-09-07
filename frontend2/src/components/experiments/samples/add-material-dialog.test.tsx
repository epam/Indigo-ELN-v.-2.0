import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeExperimentDetails, makeSample, SAMPLE_RESULTS } from '@/mocks/fixtures';

import type { ReactNode } from 'react';
import type { SampleSearchResult } from '@/lib/types/samples.ts';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

/**
 * Every request the sheet makes, path and body — where the endpoint contract is pinned, since
 * `src/lib/api/samples.ts` keeps its fetchers private and a URL is only visible from a call site.
 */
const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string, init?: RequestInit) => apiFetch(path, init) };
});

/** Ketcher is 21 MB behind a dynamic import; the sketcher is never opened here. */
vi.mock('@/lib/ketcher', () => ({
  renderStructure: vi.fn().mockResolvedValue('data:image/svg+xml,'),
  prewarmKetcher: vi.fn(),
}));

const { AddMaterialDialog } = await import('@/components/experiments/samples/add-material-dialog');

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];
const SEARCH_PATH = '/api/eln/samples/search?pageSize=100';
const MUTATE_PATH = `/api/eln/experiments/${EXPERIMENT.id}/mutate?revision=${EXPERIMENT.revision}`;

function searchResult(items = SAMPLE_RESULTS): SampleSearchResult {
  return { items, totalItems: items.length, next: null };
}

/** The last body sent to a path, parsed. */
function bodyOf(path: string): unknown {
  const call = [...apiFetch.mock.calls].reverse().find(([requested]) => requested === path);
  return call == null ? undefined : JSON.parse(call[1].body as string);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function renderDialog() {
  return render(<AddMaterialDialog open onOpenChange={() => {}} experiment={EXPERIMENT} reaction={REACTION} />, {
    wrapper,
  });
}

/** Fills the quick-search box and presses Search, which is the shortest path to results. */
async function search(term = 'aspirin') {
  await userEvent.type(screen.getByRole('searchbox', { name: 'Quick search' }), term);
  await userEvent.click(screen.getByRole('button', { name: 'Search' }));
}

beforeEach(() => {
  apiFetch.mockReset();
  apiFetch.mockImplementation((path: string) => {
    if (path.startsWith('/api/eln/samples/search')) return Promise.resolve(searchResult());
    if (path === '/api/eln/samples/importFromSearch') return Promise.resolve(makeSample({ id: 'imported-sample' }));
    if (path.includes('/mutate')) return Promise.resolve({ patch: {} });
    if (path.startsWith('/api/eln/dictionaries/')) return Promise.resolve([]);
    // The structure previews; no row is expanded, so nothing should ask.
    return Promise.resolve('<svg />');
  });
});

describe('AddMaterialDialog', () => {
  /** Unlike Analyze RXN, the form starts empty — every keystroke is not a question. */
  it('searches nothing until Search is pressed', async () => {
    renderDialog();
    await userEvent.type(screen.getByRole('searchbox', { name: 'Quick search' }), 'aspirin');

    expect(apiFetch).not.toHaveBeenCalledWith(SEARCH_PATH, expect.anything());

    await userEvent.click(screen.getByRole('button', { name: 'Search' }));

    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith(SEARCH_PATH, expect.anything()));
  });

  it('sends the term under the catalogs "All Catalogs" stands for', async () => {
    renderDialog();
    await search();

    await waitFor(() =>
      // My Materials is not additive alongside ELN, so All is the two catalogs it can add.
      expect(bodyOf(SEARCH_PATH)).toMatchObject({ catalogs: ['ELN', 'PUBCHEM'], quickSearch: 'aspirin' }),
    );
  });

  /**
   * The backend would take a request carrying only a catalog, but a whole catalog answers nothing
   * that was asked — and the first page of one is a PubChem round trip spent saying so.
   */
  it('refuses a search with no criteria', async () => {
    renderDialog();
    expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();

    await userEvent.type(screen.getByRole('searchbox', { name: 'Quick search' }), 'a');

    expect(screen.getByRole('button', { name: 'Search' })).toBeEnabled();
  });

  /**
   * The form looks filled in, but PubChem drops that filter on the way out — so Search stays
   * held until the catalog is one that can honour it.
   */
  it('does not count a filter the chosen catalog would drop', async () => {
    renderDialog();
    await userEvent.click(screen.getByRole('button', { name: 'Advanced search' }));
    await userEvent.click(screen.getByRole('radio', { name: 'Indigo ELN' }));
    await userEvent.type(screen.getByLabelText('Compound ID'), 'ASA');
    expect(screen.getByRole('button', { name: 'Search' })).toBeEnabled();

    await userEvent.click(screen.getByRole('radio', { name: 'PubChem' }));

    expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();
  });

  it('carries an advanced filter once a catalog that supports it is chosen', async () => {
    renderDialog();
    await userEvent.click(screen.getByRole('radio', { name: 'Indigo ELN' }));
    await userEvent.click(screen.getByRole('button', { name: 'Advanced search' }));
    await userEvent.type(screen.getByLabelText('Compound ID'), 'ASA');
    await userEvent.click(screen.getByRole('button', { name: 'Search' }));

    await waitFor(() =>
      expect(bodyOf(SEARCH_PATH)).toMatchObject({
        catalogs: ['ELN'],
        compoundKey: { type: 'exact', value: 'ASA' },
      }),
    );
  });

  /** PubChem takes a name, a formula or a structure; the rest would be dropped server-side. */
  it('offers no fine-grained filters while a PubChem catalog is chosen', async () => {
    renderDialog();
    await userEvent.click(screen.getByRole('button', { name: 'Advanced search' }));

    expect(screen.getByLabelText('Compound ID')).toBeDisabled();
    expect(screen.getByLabelText('Molecular Formula')).toBeEnabled();
    expect(screen.getByText('PubChem does not support fine-grained search. Use quick search instead')).toBeVisible();

    await userEvent.click(screen.getByRole('radio', { name: 'Indigo ELN' }));

    expect(screen.getByLabelText('Compound ID')).toBeEnabled();
  });

  it('appends an input row for a chosen ELN sample', async () => {
    renderDialog();
    await search();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' }));

    await waitFor(() =>
      expect(bodyOf(MUTATE_PATH)).toEqual({
        type: 'AddInput',
        anchor: REACTION.anchor,
        sampleId: SAMPLE_RESULTS[0].id,
      }),
    );
    // An ELN hit is already a sample; there is nothing to register.
    expect(apiFetch).not.toHaveBeenCalledWith('/api/eln/samples/importFromSearch', expect.anything());
  });

  it('registers a PubChem hit before adding it, since AddInput names a sample by id', async () => {
    renderDialog();
    await search();
    const pubchem = SAMPLE_RESULTS.find((sample) => sample.source === 'PUBCHEM')!;
    const label = `Add ${pubchem.name!} to the stoichiometry`;
    await screen.findByRole('button', { name: label });

    await userEvent.click(screen.getByRole('button', { name: label }));

    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith('/api/eln/samples/importFromSearch', expect.anything()));
    // The whole DTO goes back: the provider is chosen by `source`, and PubChem needs the InChI.
    expect(bodyOf('/api/eln/samples/importFromSearch')).toMatchObject({ source: 'PUBCHEM', inchi: pubchem.inchi });

    await waitFor(() =>
      expect(bodyOf(MUTATE_PATH)).toEqual({
        type: 'AddInput',
        anchor: REACTION.anchor,
        sampleId: 'imported-sample',
      }),
    );
  });

  /**
   * The sheet is mounted for the life of the page — that is what lets it slide out — so a reset
   * on close is the only thing between a visit and the last one's search. It runs on
   * `onOpenChangeComplete`, after the exit transition, which jsdom finishes immediately.
   */
  it('resets when the sheet is closed and opened again', async () => {
    const { rerender } = renderDialog();
    await search();
    await screen.findByText('Acetylsalicylic acid');

    rerender(<AddMaterialDialog open={false} onOpenChange={() => {}} experiment={EXPERIMENT} reaction={REACTION} />);
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());

    rerender(<AddMaterialDialog open onOpenChange={() => {}} experiment={EXPERIMENT} reaction={REACTION} />);

    expect(await screen.findByRole('searchbox', { name: 'Quick search' })).toHaveValue('');
    expect(screen.queryByText('Acetylsalicylic acid')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();
  });

  it('clears the form and the results', async () => {
    renderDialog();
    await search();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Clear All' }));

    expect(screen.getByRole('searchbox', { name: 'Quick search' })).toHaveValue('');
    expect(screen.queryByText('Acetylsalicylic acid')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();
  });
});
