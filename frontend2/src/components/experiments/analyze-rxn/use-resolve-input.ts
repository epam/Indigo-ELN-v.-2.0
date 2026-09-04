import { useCallback, useState } from 'react';

import { useAddSample } from '@/components/experiments/samples/use-add-sample';
import { notifyInfo } from '@/lib/toast';

import type { UUID } from '@/lib/types/common.ts';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

/**
 * Every sample already bound to an input row of this step.
 *
 * This is what disables a result row's Add button. An input row created by the scheme carries one
 * sample with no `sampleId` until something is resolved into it, so the set holds exactly the
 * samples the step has actually taken — and it is read off the model rather than remembered
 * locally, so it stays right across a reopen, and a row added by someone else's session shows as
 * taken as soon as the detail is refetched.
 */
export function boundSampleIds(reaction: Reaction): ReadonlySet<UUID> {
  const ids = new Set<UUID>();
  for (const input of reaction.inputs) {
    for (const sample of input.samples) {
      if (sample.sampleId != null) ids.add(sample.sampleId);
    }
  }
  return ids;
}

export interface ResolveInputMutations {
  /** Binds one catalog hit to one unresolved input row. */
  add: (inputAnchor: UUID, sample: SampleDTO) => void;
  /** Keyed by `sampleRowKey` — which rows are mid-add. */
  addingRows: ReadonlySet<string>;
  /** Which input anchors this dialog has bound something to, for the tab's resolved mark. */
  addedInputs: ReadonlySet<UUID>;
}

/**
 * The Analyze RXN write: `ResolveInputs`, which fills a registered sample into an input row the
 * scheme created but could not match.
 *
 * The two-step registration every catalog hit may need lives in `useAddSample`, shared with Add
 * Material; what is particular here is the mutation and `addedInputs`, which is the narrower
 * claim the tab's check mark makes — *this dialog* bound something to that input, as against
 * `boundSampleIds`, which is read off the model.
 */
export function useResolveInput(experiment: ExperimentDetails, reaction: Reaction): ResolveInputMutations {
  const [addedInputs, setAddedInputs] = useState<ReadonlySet<UUID>>(() => new Set());

  const { run, addingRows } = useAddSample(experiment);
  const reactionAnchor = reaction.anchor;

  const add = useCallback(
    (inputAnchor: UUID, sample: SampleDTO) => {
      void run(sample, (sampleId) => ({
        type: 'ResolveInputs',
        anchor: reactionAnchor,
        inputSamples: { [inputAnchor]: sampleId },
      })).then((added) => {
        if (!added) return;
        setAddedInputs((inputs) => new Set(inputs).add(inputAnchor));
        // The patch lands silently — it moves rows in a table the dialog is covering — so this
        // is the only confirmation the user gets while the dialog stays open.
        notifyInfo('Model updated with new sample');
      });
    },
    [run, reactionAnchor],
  );

  return { add, addingRows, addedInputs };
}
