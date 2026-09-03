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
 * Every request the dialog makes, path and body. This is where the endpoint contract is pinned:
 * `src/lib/api/samples.ts` keeps its fetchers private, so the URL a hook builds is only visible
 * from a call site — the same rule `projects.test.tsx` follows.
 */
const apiFetch = vi.fn();
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return { ...actual, apiFetch: (path: string, init?: RequestInit) => apiFetch(path, init) };
});

const { AnalyzeRxnDialog } = await import('@/components/experiments/analyze-rxn/analyze-rxn-dialog');

const EXPERIMENT = makeExperimentDetails();
const REACTION = EXPERIMENT.model.reactions[0];
const [REACTANT] = REACTION.inputs;
const UNRESOLVED = { [REACTANT.anchor]: 'unresolved-molfile' };

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
    if (path === '/api/eln/samples/importFromSearch') return Promise.resolve(makeSample({ id: 'imported-sample' }));
    if (path.includes('/mutate')) return Promise.resolve({ patch: {} });
    // The structure previews; the detail panel is closed, so nothing should ask.
    return Promise.resolve('<svg />');
  });
});

describe('AnalyzeRxnDialog', () => {
  it('searches the drawn structure as soon as it opens, with no interaction', async () => {
    renderDialog();

    await waitFor(() =>
      expect(apiFetch).toHaveBeenCalledWith('/api/eln/samples/search?pageSize=100', expect.anything()),
    );
    expect(bodyOf('/api/eln/samples/search?pageSize=100')).toEqual({
      // "All Catalogs" is ELN + PubChem; My Materials is not additive alongside ELN.
      catalogs: ['ELN', 'PUBCHEM'],
      structure: { type: 'SUBSTRUCTURE', query: 'unresolved-molfile' },
    });
  });

  it('searches again when the catalog changes', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('radio', { name: 'PubChem' }));

    await waitFor(() =>
      expect(bodyOf('/api/eln/samples/search?pageSize=100')).toMatchObject({ catalogs: ['PUBCHEM'] }),
    );
  });

  it('binds a chosen ELN sample to the input row it was searched for', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to the stoichiometry' }));

    const path = `/api/eln/experiments/${EXPERIMENT.id}/mutate?revision=${EXPERIMENT.revision}`;
    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith(path, expect.anything()));
    expect(bodyOf(path)).toEqual({
      type: 'ResolveInputs',
      anchor: REACTION.anchor,
      inputSamples: { [REACTANT.anchor]: SAMPLE_RESULTS[0].id },
    });
    // An ELN hit is already a sample; there is nothing to register.
    expect(apiFetch).not.toHaveBeenCalledWith('/api/eln/samples/importFromSearch', expect.anything());
  });

  it('registers a PubChem hit before binding it, since ResolveInputs names a sample by id', async () => {
    renderDialog();
    const pubchem = SAMPLE_RESULTS.find((sample) => sample.source === 'PUBCHEM')!;
    const label = `Add ${pubchem.name!} to the stoichiometry`;
    await screen.findByRole('button', { name: label });

    await userEvent.click(screen.getByRole('button', { name: label }));

    await waitFor(() => expect(apiFetch).toHaveBeenCalledWith('/api/eln/samples/importFromSearch', expect.anything()));
    // The whole DTO goes back: the provider is chosen by `source`, and PubChem needs the InChI.
    expect(bodyOf('/api/eln/samples/importFromSearch')).toMatchObject({ source: 'PUBCHEM', inchi: pubchem.inchi });

    const path = `/api/eln/experiments/${EXPERIMENT.id}/mutate?revision=${EXPERIMENT.revision}`;
    await waitFor(() =>
      expect(bodyOf(path)).toEqual({
        type: 'ResolveInputs',
        anchor: REACTION.anchor,
        inputSamples: { [REACTANT.anchor]: 'imported-sample' },
      }),
    );
  });

  it('marks a sample through the endpoint that owns the flag', async () => {
    renderDialog();
    await screen.findByText('Acetylsalicylic acid');

    await userEvent.click(screen.getByRole('button', { name: 'Add Acetylsalicylic acid to My Materials' }));

    await waitFor(() =>
      expect(apiFetch).toHaveBeenCalledWith(`/api/eln/samples/${SAMPLE_RESULTS[0].id}/mark`, expect.anything()),
    );
  });

  it('offers no Add for a sample the step already holds', async () => {
    const bound = SAMPLE_RESULTS[0].id!;
    const reaction = {
      ...REACTION,
      inputs: REACTION.inputs.map((input, index) =>
        index === 0 ? { ...input, samples: [{ ...input.samples[0], sampleId: bound }] } : input,
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
