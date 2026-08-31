import { CircleCheck, MoreHorizontal, Redo2, Undo2 } from 'lucide-react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';

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
 * Undo and redo, at the right of the tab strip.
 *
 * Both post `{"type":"Undo"}` / `{"type":"Redo"}` to `/experiments/{id}/mutate?revision={n}` when
 * they are wired. Note there is no `canUndo` on the payload to disable them from — the backend
 * answers 400 "Nothing to undo", which `apiFetch` already toasts — so the enabled version is
 * always clickable, and the keyboard shortcuts (Ctrl+Z / Ctrl+Y) land with it.
 */
export function UndoRedoButtons({ saving = false }: { saving?: boolean }) {
  return (
    // The page-level roll-up: whenever any write to this experiment is queued or running, this
    // pair goes busy. Undo and redo are the two actions that must not be taken against a revision
    // another write is about to move, so covering *them* is more than a convenient parking spot.
    <SavingOverlay pending={saving} spinner="center">
      <div className="flex items-center gap-2">
        {/* TODO(undo-redo): POST /experiments/{id}/mutate?revision={n}, plus Ctrl+Z / Ctrl+Y.
            `disabled` is unconditional until then; `saving` is already wired so enabling them
            later is one edit rather than a second pass over this file. */}
        <Button variant="outline" size="icon-lg" aria-label="Undo" disabled>
          <Undo2 />
        </Button>
        <Button variant="outline" size="icon-lg" aria-label="Redo" disabled>
          <Redo2 />
        </Button>
      </div>
    </SavingOverlay>
  );
}
