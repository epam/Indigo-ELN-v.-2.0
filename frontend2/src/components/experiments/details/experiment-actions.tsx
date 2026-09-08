import { CircleCheck, MoreHorizontal, Redo2, Undo2 } from 'lucide-react';
import { useEffect } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import { useMutateExperimentModel } from '@/lib/api/experiments';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import { canEditExperiment } from '@/lib/types/experiments.ts';

/*
 * Everything on this screen that writes is rendered and disabled, the way the Add Notebook and
 * Add Experiment buttons already are: the controls are part of the design, and leaving them out
 * would hide how much of the header is still to come. Each carries the endpoint it will call.
 *
 * Status and permission gating deliberately waits for the wiring — `ExperimentWorkflowHandlers`
 * allows Complete and Cancel only from OPEN/REOPEN, and Reopen only from the terminal statuses,
 * but a gate written now would be untested code guarding buttons that do nothing.
 */

/** The primary action and the overflow menu, at the right of the header's first row. */
export function ExperimentActions() {
  return (
    <>
      {/* TODO(workflow): POST /experiments/{id}/workflow/complete. */}
      <Button size="lg" className="rounded-md" disabled>
        <CircleCheck />
        Complete Experiment
      </Button>

      <Menu>
        <MenuTrigger
          render={
            <Button variant="outline" size="icon-lg" aria-label="More actions">
              <MoreHorizontal />
            </Button>
          }
        />
        <MenuContent>
          {/* TODO(workflow): POST /experiments/{id}/workflow/reopen. */}
          <MenuItem disabled>Reopen Experiment</MenuItem>
          {/* TODO(workflow): POST /experiments/{id}/workflow/cancel. */}
          <MenuItem disabled>Cancel Experiment</MenuItem>
          {/* TODO(print): POST /experiments/{id}/print — apiDownload is GET-only today. */}
          <MenuItem disabled>Print Report</MenuItem>
          {/* TODO(export-sdf): GET /experiments/{id}/exportSdf. */}
          <MenuItem disabled>Export SDF</MenuItem>
        </MenuContent>
      </Menu>
    </>
  );
}

/**
 * What a chord has to *not* be inside for this to claim it. Each of these owns Ctrl+Z itself: a
 * text field and a `<select>` have the browser's own undo, `RichTextEditor` has ProseMirror's
 * history, and the sketcher has Ketcher's — which is reached through `role="dialog"`, since every
 * dialog in the app is a Base UI popup carrying it, Global Search and Add Material included.
 */
const OWNS_UNDO = 'input, textarea, select, [contenteditable="true"], [role="dialog"]';

/**
 * Undo and redo, at the right of the tab strip.
 *
 * Both post `{"type":"Undo"}` / `{"type":"Redo"}` to `/experiments/{id}/mutate?revision={n}`,
 * through the same hook and the same `experimentWrite` scope as every other model mutation — so
 * an undo queues behind the on-blur field saves rather than racing them, and the diff it answers
 * with lands in the cached detail the way any other patch does.
 *
 * **There is no `canUndo`.** Neither the experiment payload nor `MutationResponse` carries one,
 * so an enabled button cannot promise there is anything to undo: pressing with an empty stack
 * answers 400 `Nothing to undo`, which `apiFetch` toasts centrally. The enabled/disabled state
 * therefore says only whether this experiment is writable at all — `canEditExperiment`, the same
 * gate every other editable surface asks, and the same one `ExperimentService.mutateModel`
 * enforces with `ensureAccess(EDIT_EXPERIMENTS)`.
 *
 * Undo is also **per user** on the backend: `AbstractUndoHelper` skips revisions somebody else
 * made, so this never takes back a colleague's edit.
 */
export function UndoRedoButtons({ experiment, saving = false }: { experiment: ExperimentDetails; saving?: boolean }) {
  // `mutate`, not `mutateAsync`: nothing here awaits the result, and a failure has already been
  // reported by `apiFetch` before the promise settles.
  const { mutate: send } = useMutateExperimentModel(experiment);
  const canEdit = canEditExperiment(experiment);

  /**
   * Ctrl/Cmd+Z and Ctrl/Cmd+Y, plus Ctrl/Cmd+Shift+Z — Cmd+Y is not redo on macOS, so that third
   * chord is what makes the pair work there.
   *
   * On `document`, because the shortcut belongs to the page rather than to the two buttons, and
   * this component is mounted exactly when the page is showing an experiment.
   *
   * No `event.repeat` guard: the first press flips `saving` within a tick, and the `saving` bail
   * below then throttles a held key to one undo per round trip.
   */
  useEffect(() => {
    if (!canEdit || saving) return;

    function handleKeyDown(event: KeyboardEvent) {
      if (!(event.ctrlKey || event.metaKey) || event.altKey) return;
      // Something more specific already claimed the chord.
      if (event.defaultPrevented) return;
      if (event.target instanceof Element && event.target.closest(OWNS_UNDO)) return;

      const key = event.key.toLowerCase();
      const type = key === 'y' || (key === 'z' && event.shiftKey) ? 'Redo' : key === 'z' ? 'Undo' : null;
      if (type === null) return;

      event.preventDefault();
      send({ type });
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [canEdit, saving, send]);

  return (
    // The page-level roll-up: whenever any write to this experiment is queued or running, this
    // pair goes busy. Undo and redo are the two actions that must not be taken against a revision
    // another write is about to move, so covering *them* is more than a convenient parking spot —
    // and since an undo carries `experimentWrite`'s key itself, this is also what stops a second
    // click from queueing a second undo behind the first.
    <SavingOverlay pending={saving} spinner="center">
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon-lg"
          aria-label="Undo"
          disabled={!canEdit}
          onClick={() => send({ type: 'Undo' })}
        >
          <Undo2 />
        </Button>
        <Button
          variant="outline"
          size="icon-lg"
          aria-label="Redo"
          disabled={!canEdit}
          onClick={() => send({ type: 'Redo' })}
        >
          <Redo2 />
        </Button>
      </div>
    </SavingOverlay>
  );
}
