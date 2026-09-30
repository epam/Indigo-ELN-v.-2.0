import { useCallback, useState } from 'react';

import { useMutateExperimentModel } from '@/lib/api/experiments';
import { sampleRowKey } from '@/lib/search';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

export interface AddSample {
  /**
   * Runs the mutation that puts `sample` into the model, with a spinner on its row meanwhile.
   * Resolves `true` once the model has taken it, `false` if it failed.
   */
  run: (sample: SampleDTO, mutation: ModelMutation) => Promise<boolean>;
  /** Which rows are mid-add, keyed by `sampleRowKey` — what puts a spinner on one. */
  addingRows: ReadonlySet<string>;
}

/**
 * Putting a catalog hit into the reaction model, whichever dialog asks.
 *
 * One step: the mutations that take a sample (`AddInput`, `ResolveInputs`) carry the whole
 * `SampleDTO`, and the backend imports its compound from the catalog itself.
 *
 * Which mutation that is belongs to the caller — `ResolveInputs` binds the hit to a row the
 * scheme already created, `AddInput` appends a new one — so it is passed in rather than decided
 * here. Both go through `useMutateExperimentModel`, so they join `experimentWrite`'s scope and
 * queue behind the stoichiometry table's cell saves instead of racing them, and their response
 * patches the cached detail, which is how the table behind the dialog fills itself in.
 *
 * On failure nothing local changes: `apiFetch` has already raised the toast, the row keeps its
 * enabled Add button. Nothing here is optimistic, which is why `run` reports the outcome rather
 * than the caller assuming one.
 */
export function useAddSample(experiment: ExperimentDetails): AddSample {
  const [addingRows, setAddingRows] = useState<ReadonlySet<string>>(() => new Set());

  const { mutateAsync: runMutation } = useMutateExperimentModel(experiment);

  const run = useCallback(
    async (sample: SampleDTO, mutation: ModelMutation) => {
      const row = sampleRowKey(sample);
      setAddingRows((rows) => new Set(rows).add(row));

      try {
        await runMutation(mutation);
        return true;
      } catch {
        // Deliberately silent: apiFetch toasts every failed request, and repeating it here
        // would say the same thing twice.
        return false;
      } finally {
        setAddingRows((rows) => {
          const next = new Set(rows);
          next.delete(row);
          return next;
        });
      }
    },
    [runMutation],
  );

  return { run, addingRows };
}
