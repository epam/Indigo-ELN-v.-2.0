import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { makeExperimentDetails, SAMPLE_RESULTS } from '@/mocks/fixtures';

import type { ReactNode } from 'react';
import type { Page } from '@/lib/types/common.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

const fetchAuthSession = vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

/**
 * Every request the dialog makes, path and body. This is where the endpoint contract is pinned:
 * `src/lib/api/samples.ts` keeps its fetchers private, so the URL a hook builds is only visible
 * from a call site — the same rule `projects.test.tsx` follows.
 */
const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string, init?: { json?: unknown }) => apiFetch(path, init) };
});

/** Ketcher is 21 MB behind a dynamic import; opening the dialog only prewarms it. */
vi.mock('@/lib/ketcher', () => ({
  renderStructure: vi.fn().mockResolvedValue('data:image/svg+xml,'),
  prewarmKetcher: vi.fn(),
}));

const { AnalyzeRxnDialog } = await import('@/components/experiments/analyze-rxn/analyze-rxn-dialog');

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];
const [REACTANT] = REACTION.inputs;
const UNRESOLVED = { [REACTANT.anchor]: 'unresolved-molfile' };

const SEARCH_PATH = '/api/eln/samples/search?pageNo=0&pageSize=100';

function searchResult(items = SAMPLE_RESULTS): Page<SampleDTO> {
  return { pageNo: 0, pageSize: 100, totalItems: items.length, totalPages: 1, hasMore: false, items };
}

/** The last JSON payload sent to a path — `apiFetch` serialises it, so this is the object. */
function bodyOf(path: string): unknown {
  const call = [...apiFetch.mock.calls].reverse().find(([requested]) => requested === path);
  return call == null ? undefined : call[1].json;
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function renderDialog() {
  return render(
    <AnalyzeRxnDialog
      open
      onOpenChange={() => {}}
      experiment={EXPERIMENT}
      reaction={REACTION}
      step={0}
      unresolvedInputs={UNRESOLVED}
    />,
    { wrapper },
  );
}

beforeEach(() => {
  apiFetch.mockReset();
  apiFetch.mockImplementation((path: string) => {
    if (path.startsWith('/api/eln/samples/search')) return Promise.resolve(searchResult());
    if (path.includes('/mutate')) return Promise.resolve({ patch: {} });
    // The structure previews; the detail panel is closed, so nothing should ask.
    return Promise.resolve('<svg />');
  });
});

describe('AnalyzeRxnDialog', () => {
  it('searches the drawn structure as soon as it opens, with no interaction', async () => {
    renderDialog();

    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith(SEARCH_PATH, expect.anything()));
    expect(bodyOf(SEARCH_PATH)).toEqual({
      catalog: 'SRS',
      structure: { type: 'SUBSTRUCTURE', query: 'unresolved-molfile' },
    });
  });

  it('searches again when the catalog changes', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('radio', { name: 'PubChem' }));

    await waitFor(() => expect(bodyOf(SEARCH_PATH)).toMatchObject({ catalog: 'PUBCHEM' }));
  });

  it('binds a chosen sample to the input row it was searched for, sending the hit back whole', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' }));

    const path = `/api/eln/experiments/${EXPERIMENT.id}/mutate?revision=${EXPERIMENT.revision}`;
    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith(path, expect.anything()));
    expect(bodyOf(path)).toEqual({
      type: 'ResolveInputs',
      anchor: REACTION.anchor,
      inputSamples: { [REACTANT.anchor]: SAMPLE_RESULTS[0] },
      createdSampleAnchors: { [REACTANT.anchor]: expect.any(String) },
    });
  });

  it('marks a sample through the endpoint that owns the flag', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to My Materials' }));

    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith('/api/eln/samples/mark', expect.anything()));
    expect(bodyOf('/api/eln/samples/mark')).toEqual(SAMPLE_RESULTS[0]);
  });

  it('offers no Add for a sample the step already holds', async () => {
    const { source, sampleKey } = SAMPLE_RESULTS[0];
    const reaction = {
      ...REACTION,
      inputs: REACTION.inputs.map((input, index) =>
        index === 0 ? { ...input, samples: [{ ...input.samples[0], sampleSource: source, sampleKey }] } : input,
      ),
    };

    render(
      <AnalyzeRxnDialog
        open
        onOpenChange={() => {}}
        experiment={EXPERIMENT}
        reaction={reaction}
        step={0}
        unresolvedInputs={UNRESOLVED}
      />,
      { wrapper },
    );

    const add = await screen.findByRole('button', { name: 'Acetylsalicylic acid is already in the stoichiometry' });
    expect(add).toBeDisabled();
    // The rest of the results are unaffected.
    expect(screen.getByRole('button', { name: 'Add Salicylic acid to the stoichiometry' })).toBeEnabled();
  });
});
