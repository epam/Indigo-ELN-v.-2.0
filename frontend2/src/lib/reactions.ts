import type { UUID } from '@/lib/types/common.ts';
import type { Reaction } from '@/lib/types/reactions.ts';

/**
 * Every sample already bound to an input row of this step.
 *
 * This is what disables a result row's Add button, in both catalog sheets. An input row created by
 * the scheme carries one sample with no `sampleId` until something is resolved into it, so the set
 * holds exactly the samples the step has actually taken — and it is read off the model rather than
 * remembered locally, so it stays right across a reopen, and a row added by someone else's session
 * shows as taken as soon as the detail is refetched.
 */
export function getAllInputSampleIds(reaction: Reaction): ReadonlySet<UUID> {
  const ids = new Set<UUID>();
  for (const input of reaction.inputs) {
    for (const sample of input.samples) {
      if (sample.sampleId != null) ids.add(sample.sampleId);
    }
  }
  return ids;
}
