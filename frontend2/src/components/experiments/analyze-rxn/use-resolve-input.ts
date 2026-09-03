import { useCallback, useState } from 'react';

import { useMutateExperimentModel } from '@/lib/api/experiments';
import { useImportSample } from '@/lib/api/samples';
import { notifyInfo } from '@/lib/toast';

import type { UUID } from '@/lib/types/common.ts';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

/**
 * What identifies a result row for the purpose of showing a spinner on it.
 *
 * A PubChem hit has no `id` — that is the whole reason `importFromSearch` exists — so it is
 * identified by the catalog that produced it plus that catalog's own key (the CID). Not by array
 * index: a second page arriving renumbers nothing, but a row moving between tabs would.
 */
export function sampleRowKey(sample: SampleDTO): string {
  return sample.id ?? `${sample.source}:${sample.compoundKey ?? sample.molFormula}`;
}

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
 * The Analyze RXN write, in the shape `useStoichiometryMutations` gives the tables: one place
 * that owns the mutations and a `Set` of what is currently busy.
 *
 * Adding is two steps, as in indigo-frontend. A hit that is not in the ELN yet is registered
 * first (`importFromSearch`), because `ResolveInputs` names a sample by id and a PubChem row does
 * not have one; a hit that is already an ELN sample skips straight to the mutation. Sequenced
 * with `await` rather than nested callbacks so the failure of either step lands in one place.
 *
 * `ResolveInputs` goes through `useMutateExperimentModel`, so it joins `experimentWrite`'s scope
 * and queues behind the stoichiometry table's cell saves instead of racing them, and its response
 * patches the cached detail — which is how the table behind the dialog fills itself in.
 *
 * On failure nothing local changes: `apiFetch` has already raised the toast, the row keeps its
 * enabled Add button, and the tab is not marked resolved. Nothing here is optimistic.
 */
export function useResolveInput(experiment: ExperimentDetails, reaction: Reaction): ResolveInputMutations {
  const [addingRows, setAddingRows] = useState<ReadonlySet<string>>(() => new Set());
  const [addedInputs, setAddedInputs] = useState<ReadonlySet<UUID>>(() => new Set());

  const { mutateAsync: runImport } = useImportSample();
  const { mutateAsync: runMutation } = useMutateExperimentModel(experiment);
  const reactionAnchor = reaction.anchor;

  const add = useCallback(
    (inputAnchor: UUID, sample: SampleDTO) => {
      const row = sampleRowKey(sample);
      setAddingRows((rows) => new Set(rows).add(row));

      void (async () => {
        try {
          // `importFromSearch` answers with the persisted sample, so `id` is set on the way back
          // out even though the DTO type has to allow its absence on the way in.
          const sampleId = sample.id ?? (await runImport(sample)).id!;
          await runMutation({
            type: 'ResolveInputs',
            anchor: reactionAnchor,
            inputSamples: { [inputAnchor]: sampleId },
          });
          setAddedInputs((inputs) => new Set(inputs).add(inputAnchor));
          // The patch lands silently — it moves rows in a table the dialog is covering — so this
          // is the only confirmation the user gets while the dialog stays open.
          notifyInfo('Model updated with new sample');
        } catch {
          // Deliberately silent: apiFetch toasts every failed request, and repeating it here
          // would say the same thing twice.
        } finally {
          setAddingRows((rows) => {
            const next = new Set(rows);
            next.delete(row);
            return next;
          });
        }
      })();
    },
    [runImport, runMutation, reactionAnchor],
  );

  return { add, addingRows, addedInputs };
}
