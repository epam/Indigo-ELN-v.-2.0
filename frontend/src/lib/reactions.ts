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

/** Mirrors `Reaction.getPrecursorReactantIds()`: the `sampleKey` of every reactant sample that has one. */
export function getPrecursorReactantIds(reaction: Reaction): string[] {
  return reaction.inputs
    .filter((input) => input.role === 'REACTANT')
    .flatMap((input) => input.samples)
    .flatMap((sample) => sample.sampleKey ?? []);
}

/**
 * The node with this anchor, or undefined when there is none — how a row finds its counterpart in
 * the model before the last patch. Undefined for a list that is itself absent there.
 */
export function findByAnchor<T extends { anchor: string }>(
  items: readonly T[] | undefined,
  anchor: string,
): T | undefined {
  return items?.find((item) => item.anchor === anchor);
}

/** Why the reaction scheme refuses a drawing with no arrow: `SetScheme` takes a rxnfile. */
export const NOT_A_REACTION = 'Draw a reaction, not a single molecule';

/** Its mirror, for a batch: `SetOutputMolfile` takes one compound's molfile. */
export const NOT_A_MOLECULE = 'Draw a single molecule, not a reaction';
