import { useCallback, useState } from 'react';

import { sampleRowKey } from '@/components/experiments/samples/sample-row-key';
import { useMutateExperimentModel } from '@/lib/api/experiments';
import { useImportSample } from '@/lib/api/samples';

import type { UUID } from '@/lib/types/common.ts';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

export interface AddSample {
  /**
   * Registers the hit if it needs registering, then runs the mutation built from the id it ends
   * up with. Resolves `true` once the model has taken it, `false` if either step failed.
   */
  run: (sample: SampleDTO, toMutation: (sampleId: UUID) => ModelMutation) => Promise<boolean>;
  /** Which rows are mid-add, keyed by `sampleRowKey` — what puts a spinner on one. */
  addingRows: ReadonlySet<string>;
}

/**
 * Putting a catalog hit into the reaction model, which is two steps whichever dialog asks.
 *
 * A hit that is not in the ELN yet is registered first (`importFromSearch`), because every
 * mutation that takes a sample names it by id and a PubChem row does not have one; a hit that is
 * already an ELN sample skips straight to the mutation. Sequenced with `await` rather than
 * nested callbacks so the failure of either step lands in one place.
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

  const { mutateAsync: runImport } = useImportSample();
  const { mutateAsync: runMutation } = useMutateExperimentModel(experiment);

  const run = useCallback(
    async (sample: SampleDTO, toMutation: (sampleId: UUID) => ModelMutation) => {
      const row = sampleRowKey(sample);
      setAddingRows((rows) => new Set(rows).add(row));

      try {
        // `importFromSearch` answers with the persisted sample, so `id` is set on the way back
        // out even though the DTO type has to allow its absence on the way in.
        const sampleId = sample.id ?? (await runImport(sample)).id!;
        await runMutation(toMutation(sampleId));
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
    [runImport, runMutation],
  );

  return { run, addingRows };
}
