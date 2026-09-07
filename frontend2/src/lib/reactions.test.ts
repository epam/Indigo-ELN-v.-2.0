import { describe, expect, it } from 'vitest';

import { getAllInputSampleIds } from '@/lib/reactions';
import { makeReaction, makeReactionInput, makeReactionInputSample } from '@/mocks/fixtures';

describe('getAllInputSampleIds', () => {
  it('collects the sample ids from every input row of the step', () => {
    const reaction = makeReaction({
      inputs: [
        makeReactionInput('d0000000-0000-4000-8000-000000000001', {
          samples: [makeReactionInputSample('e0000000-0000-4000-8000-00000000000a', { sampleId: 'sample-1' })],
        }),
        makeReactionInput('d0000000-0000-4000-8000-000000000002', {
          samples: [
            makeReactionInputSample('e0000000-0000-4000-8000-00000000000b', { sampleId: 'sample-2' }),
            makeReactionInputSample('e0000000-0000-4000-8000-00000000000c', { sampleId: 'sample-3' }),
          ],
        }),
      ],
    });

    expect(getAllInputSampleIds(reaction)).toEqual(new Set(['sample-1', 'sample-2', 'sample-3']));
  });

  /** The state an unresolved row is in: a sample exists, but nothing is bound to it yet. */
  it('skips a sample with nothing bound to it', () => {
    const reaction = makeReaction({
      inputs: [
        makeReactionInput('d0000000-0000-4000-8000-000000000001', {
          samples: [makeReactionInputSample('e0000000-0000-4000-8000-00000000000a')],
        }),
      ],
    });

    expect(getAllInputSampleIds(reaction)).toEqual(new Set());
  });

  it('has nothing to collect from a step with no inputs', () => {
    expect(getAllInputSampleIds(makeReaction({ inputs: [] }))).toEqual(new Set());
  });
});
