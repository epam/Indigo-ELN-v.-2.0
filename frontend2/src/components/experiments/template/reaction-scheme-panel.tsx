import { useState } from 'react';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';
import { SavingOverlay } from '@/components/common/saving-overlay';
import { AnalyzeRxnDialog } from '@/components/experiments/analyze-rxn/analyze-rxn-dialog';
import { useMutateExperimentModel } from '@/lib/api/experiments';
import { canEditExperiment } from '@/lib/types/experiments.ts';

import type { UUID } from '@/lib/types/common.ts';
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
 * What the response's `unresolvedInputs` names, though, is acted on: the molecules that got an
 * input row but no compound behind it. `AnalyzeRxnDialog` opens on them.
 *
 * On failure the sketcher stays open with the drawing in it (`StructureEditorDialog` awaits
 * this mutation), nothing is written to the cache, and the frame keeps showing the structure
 * the server last confirmed.
 */
export function ReactionSchemePanel({
  experiment,
  reaction,
  step,
}: {
  experiment: ExperimentDetails;
  reaction: Reaction;
  /** Zero-based index of this step, for the Analyze RXN title. */
  step: number;
}) {
  const mutate = useMutateExperimentModel(experiment);
  const canEdit = canEditExperiment(experiment);

  /**
   * Read here rather than in `applyMutationResponse` so the api module stays free of UI. By the
   * time this is set the patch has already been applied, so the `reaction` prop the dialog is
   * handed is the one that actually contains the new input rows.
   */
  const [unresolvedInputs, setUnresolvedInputs] = useState<Record<UUID, string> | null>(null);

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
          if (!next) return;
          const response = await mutate.mutateAsync({
            type: 'SetScheme',
            anchor: reaction.anchor,
            rxnFile: next.structure,
          });
          // `SetScheme` reports the map on essentially every edit, empty when everything matched.
          if (response.unresolvedInputs && Object.keys(response.unresolvedInputs).length > 0) {
            setUnresolvedInputs(response.unresolvedInputs);
          }
        }}
      />

      {/* Mounted only while there is something to resolve, so each run starts on a clean tab. */}
      {unresolvedInputs != null && (
        <AnalyzeRxnDialog
          open
          onOpenChange={(open) => {
            if (!open) setUnresolvedInputs(null);
          }}
          experiment={experiment}
          reaction={reaction}
          step={step}
          unresolvedInputs={unresolvedInputs}
        />
      )}
    </SavingOverlay>
  );
}
