import { useCallback, useState } from 'react';

import { useMutateExperimentModel } from '@/lib/api/experiments';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Mutation } from '@/lib/types/mutations.ts';

/** Addresses one cell for the purposes of its spinner: the row's anchor plus the column id. */
export function cellId(anchor: string, column: string): string {
  return `${anchor}:${column}`;
}

export interface StoichiometryMutations {
  /** Fires one mutation and marks `cell` busy until it settles. */
  save: (cell: string, mutation: Mutation) => void;
  /** Which cells are mid-save — what each cell's `SavingOverlay` reads. */
  savingCells: ReadonlySet<string>;
  /** `newNode -> oldNode` from the last patch; drives the recalculation flash. */
  updatedNodes: ReadonlyMap<unknown, unknown>;
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
 * No optimistic update, matching the Angular original and the rest of this screen — a cell
 * shows what the server last confirmed, and a failed save leaves it there. `apiFetch` has
 * already raised the error toast, so a rejection needs no handling beyond clearing the
 * spinner, which `onSettled` does whichever way the mutation went.
 */
export function useStoichiometryMutations(experiment: ExperimentDetails): StoichiometryMutations {
  const [savingCells, setSavingCells] = useState<ReadonlySet<string>>(() => new Set());
  const [updatedNodes, setUpdatedNodes] = useState<ReadonlyMap<unknown, unknown>>(() => new Map());

  const mutate = useMutateExperimentModel(experiment, setUpdatedNodes);
  const { mutate: run } = mutate;

  const save = useCallback(
    (cell: string, mutation: Mutation) => {
      setSavingCells((cells) => new Set(cells).add(cell));
      run(mutation, {
        onSettled: () =>
          setSavingCells((cells) => {
            const next = new Set(cells);
            next.delete(cell);
            return next;
          }),
      });
    },
    [run],
  );

  return { save, savingCells, updatedNodes };
}
