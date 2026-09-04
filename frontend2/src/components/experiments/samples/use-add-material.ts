import { useCallback } from 'react';

import { useAddSample } from '@/components/experiments/samples/use-add-sample';
import { notifyInfo } from '@/lib/toast';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

export interface AddMaterialMutations {
  /** Appends an input row for one catalog hit. */
  add: (sample: SampleDTO) => void;
  /** Keyed by `sampleRowKey` — which rows are mid-add. */
  addingRows: ReadonlySet<string>;
}

/**
 * The Add Material write: `AddInput`, which appends an input row carrying a registered compound,
 * as against `AddEmptyInput`'s `UNKNOWN` one.
 *
 * The step's own rows are not touched — this is an append, so unlike Analyze RXN there is no
 * anchor to resolve into and nothing to mark as done. The patch that comes back adds the row to
 * the table behind the sheet; the toast is the only confirmation the user gets while the sheet
 * stays open over it, which is the same reason `useResolveInput` raises one.
 */
export function useAddMaterial(experiment: ExperimentDetails, reaction: Reaction): AddMaterialMutations {
  const { run, addingRows } = useAddSample(experiment);
  const reactionAnchor = reaction.anchor;

  const add = useCallback(
    (sample: SampleDTO) => {
      void run(sample, (sampleId) => ({ type: 'AddInput', anchor: reactionAnchor, sampleId })).then((added) => {
        if (added) notifyInfo('Model updated with new sample');
      });
    },
    [run, reactionAnchor],
  );

  return { add, addingRows };
}
