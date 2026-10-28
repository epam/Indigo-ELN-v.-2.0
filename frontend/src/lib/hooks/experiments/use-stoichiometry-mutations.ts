import { useCallback, useState } from 'react';

import { useModelPatch, useMutateExperimentModel } from '@/lib/api/experiments';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { ExperimentModel } from '@/lib/types/reactions.ts';

/** Addresses one cell for the purposes of its spinner: the row's anchor plus the column id. */
export function cellId(anchor: string, column: string): string {
  return `${anchor}:${column}`;
}

export interface StoichiometryMutations {
  /**
   * Fires one mutation and marks `cell` busy until it settles. Resolves to whether it landed, for
   * the one kind of caller that has to wait — a dialog that closes only on success.
   */
  save: (cell: string, mutation: ModelMutation) => Promise<boolean>;
  /** Which cells are mid-save — what each cell's `SavingOverlay` reads. */
  savingCells: ReadonlySet<string>;
  /**
   * The model before the last patch to this experiment, whoever sent it — undo, the scheme,
   * another table — so every table flashes what it moved. The current model when no patch has
   * landed since mount, so every value equals itself and nothing flashes.
   */
  previousModel: ExperimentModel;
  /** Cells whose user-entered value the last patch overwrote — see `ModelPatch.conflicts`. */
  conflicts: ReadonlySet<string>;
  /** Counts patches — see `ModelPatch.generation`. */
  generation: number;
}

/**
 * One mutation instance for the whole table, with the busy state tracked per cell.
 *
 * The obvious alternative — a `useMutation` per cell, the way `ExperimentDetailsPanel` gives
 * each of its seven fields its own — does not scale here: a full table is sixteen columns
 * across a dozen rows plus their samples, and every one of those hooks would subscribe to the
 * mutation cache and re-render on every unrelated write. A `Set` of busy cell ids gives the
 * same "freeze only the control being saved" behaviour for one subscription.
 *
 * A `Set` rather than a single id because the set really can hold more than one: writes are
 * serialised by `experimentWrite`'s scope, so a user tabbing quickly leaves the first cell
 * queued while the second is already asked for, and both must stay frozen.
 *
 * Each cell is cleared by its own `mutateAsync` promise, not by a per-call `onSettled` on
 * `mutate`: TanStack runs per-call callbacks only for the latest `mutate` on an instance, so
 * clicking Register down a column would leave every batch but the last spinning forever.
 *
 * No optimistic update, matching the rest of this screen — a cell
 * shows what the server last confirmed, and a failed save leaves it there. `apiFetch` has
 * already raised the error toast, so a rejection needs no handling beyond clearing the
 * spinner, which the `finally` does whichever way the mutation went.
 */
export function useStoichiometryMutations(experiment: ExperimentDetails): StoichiometryMutations {
  const [savingCells, setSavingCells] = useState<ReadonlySet<string>>(() => new Set());
  const patch = useModelPatch(experiment.id);

  const mutate = useMutateExperimentModel(experiment);
  const { mutateAsync: run } = mutate;

  const save = useCallback(
    async (cell: string, mutation: ModelMutation) => {
      setSavingCells((cells) => new Set(cells).add(cell));
      try {
        await run(mutation);
        return true;
      } catch {
        // Deliberately silent: apiFetch toasts every failed request.
        return false;
      } finally {
        setSavingCells((cells) => {
          const next = new Set(cells);
          next.delete(cell);
          return next;
        });
      }
    },
    [run],
  );

  return {
    save,
    savingCells,
    previousModel: patch.previous ?? experiment.model,
    conflicts: patch.conflicts,
    generation: patch.generation,
  };
}
