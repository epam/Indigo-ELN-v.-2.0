import { describe, expect, it } from 'vitest';

import { getAllInputSampleKeys, getPrecursorReactantIds } from '@/lib/reactions';
import { makeReaction, makeReactionInput, makeReactionInputSample } from '@/mocks/fixtures';

describe('getAllInputSampleKeys', () => {
  it('collects the sample keys from every input row of the step', () => {
    const reaction = makeReaction({
      inputs: [
        makeReactionInput('inp00001', {
          samples: [
            makeReactionInputSample('ins0000a', {
              sampleSource: 'SRS',
              sampleKey: 'STR-1',
            }),
          ],
        }),
        makeReactionInput('inp00002', {
          samples: [
            makeReactionInputSample('ins0000b', {
              sampleSource: 'SRS',
              sampleKey: 'STR-2',
            }),
            makeReactionInputSample('ins0000c', {
              sampleSource: 'PUBCHEM',
              sampleKey: '2244',
            }),
          ],
        }),
      ],
    });

    expect(getAllInputSampleKeys(reaction)).toEqual(new Set(['SRS:STR-1', 'SRS:STR-2', 'PUBCHEM:2244']));
  });

  /** The state an unresolved row is in: a `VIRTUAL` sample exists, but nothing is bound to it yet. */
  it('skips a sample with nothing bound to it', () => {
    const reaction = makeReaction({
      inputs: [
        makeReactionInput('inp00001', {
          samples: [makeReactionInputSample('ins0000a')],
        }),
      ],
    });

    expect(getAllInputSampleKeys(reaction)).toEqual(new Set());
  });

  it('has nothing to collect from a step with no inputs', () => {
    expect(getAllInputSampleKeys(makeReaction({ inputs: [] }))).toEqual(new Set());
  });
});

describe('getPrecursorReactantIds', () => {
  it('collects the keyed samples of reactant rows only', () => {
    const reaction = makeReaction({
      inputs: [
        makeReactionInput('inp00001', {
          samples: [
            makeReactionInputSample('ins0000a', { sampleSource: 'SRS', sampleKey: 'STR-1' }),
            makeReactionInputSample('ins0000b'),
          ],
        }),
        makeReactionInput('inp00002', {
          role: 'SOLVENT',
          samples: [makeReactionInputSample('ins0000c', { sampleSource: 'SRS', sampleKey: 'STR-2' })],
        }),
      ],
    });

    expect(getPrecursorReactantIds(reaction)).toEqual(['STR-1']);
  });
});
