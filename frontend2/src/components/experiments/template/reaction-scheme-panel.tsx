import { SchemeEditor } from '@/components/chemistry/scheme-editor';
import { SavingOverlay } from '@/components/common/saving-overlay';
import { useMutateExperimentModel } from '@/lib/api/experiments';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';

/**
 * One reaction step's drawn scheme, over the reaction's own rxnfile.
 *
 * Saving sends a `SetScheme` mutation, and the backend answers with a diff rather than a
 * document: it re-reads the drawing, matches each molecule against the rows it already has,
 * and creates, deletes and repositions inputs and outputs to match. So a scheme edit patches
 * most of the model, not just this one field, and `useMutateExperimentModel` applies all of
 * it at once.
 *
 * The scheme then redraws itself — the patch rewrites `reaction.rxnfile`, that flows back in
 * as `value`, and `SchemeEditor` renders it off the warm Indigo worker. The server also
 * returns rendered SVGs in `reactionImages`, which indigo-frontend displays; they are ignored
 * here, along with the `/datamodel/reactions/{anchor}/picture` endpoint behind them.
 *
 * On failure the sketcher stays open with the drawing in it (`StructureEditorDialog` awaits
 * this mutation), nothing is written to the cache, and the frame keeps showing the structure
 * the server last confirmed.
 */
export function ReactionSchemePanel({ experiment, reaction }: { experiment: ExperimentDetails; reaction: Reaction }) {
  const mutate = useMutateExperimentModel(experiment);
  const canEdit = experiment.currentPermissions.includes('EDIT_EXPERIMENTS');

  return (
    // The dialog covers the frame during a save, but a mutation queued behind another write
    // is pending with nothing open, and that is what this shows.
    <SavingOverlay pending={mutate.isPending} spinner="top">
      <SchemeEditor
        value={reaction.rxnfile ?? null}
        disabled={mutate.isPending || !canEdit}
        // mutateAsync, not mutate: the dialog needs a promise to know when to close, and it
        // is the only caller here and always awaits — so the rejection is caught there
        // rather than leaking as an unhandled one.
        onChange={async (next) => {
          if (next) await mutate.mutateAsync({ type: 'SetScheme', anchor: reaction.anchor, rxnFile: next.structure });
        }}
      />
    </SavingOverlay>
  );
}
