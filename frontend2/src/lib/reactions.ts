import { sampleKeyOf } from '@/lib/search';

import type { Reaction } from '@/lib/types/reactions.ts';

/**
 * Every sample already bound to an input row of this step, keyed like `sampleRowKey`.
 *
 * This is what disables a result row's Add button, in both catalog sheets. An input row created by
 * the scheme carries one `VIRTUAL` sample with no `sampleKey` until something is resolved into it,
 * so the set holds exactly the samples the step has actually taken — and it is read off the model
 * rather than remembered locally, so it stays right across a reopen, and a row added by someone
 * else's session shows as taken as soon as the detail is refetched.
 */
export function getAllInputSampleKeys(reaction: Reaction): ReadonlySet<string> {
  const keys = new Set<string>();
  for (const input of reaction.inputs) {
    for (const sample of input.samples) {
      if (sample.sampleKey != null) keys.add(sampleKeyOf(sample.sampleSource, sample.sampleKey));
    }
  }
  return keys;
}
