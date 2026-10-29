import { Ban, CircleCheck, Printer, Redo2, RotateCcw, Send, Signature, Undo2 } from 'lucide-react';
import { useEffect, useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { FormDialog } from '@/components/common/form-dialog';
import { SignDialog } from '@/components/experiments/details/sign-dialog';
import { Button, type ButtonVariant } from '@/components/ui/button';
import {
  useExperimentSaving,
  useExperimentWorkflow,
  useMutateExperimentModel,
  usePrintReport,
} from '@/lib/api/experiments';
import { notifyInfo } from '@/lib/toast';
import type { ExperimentDetails, WorkflowAction } from '@/lib/types/experiments.ts';
import { canEditExperiment, canRunWorkflow, workflowActionsFor } from '@/lib/types/experiments.ts';

/**
 * How each transition presents itself, and what happens before it fires. The statuses that decide
 * whether it is offered at all live with the endpoint table in `lib/types/experiments.ts` — this is
 * only the wording and the chrome.
 *
 * `done` is toasted on success. The backend answers with the experiment rather than a message, so
 * these are the client's own wording.
 *
 * Every transition asks before it fires: the two submits through the template picker, the rest
 * through `confirm`. Neither confirm label is left to `FormDialog`'s defaults — "Cancel Experiment"
 * beside a button called "Cancel" is unreadable, so the buttons say what they do.
 */
const WORKFLOW: Record<
  WorkflowAction,
  {
    label: string;
    icon: typeof CircleCheck;
    variant: ButtonVariant;
    done: string;
    /** Opens the signature-template picker first and passes the choice through. */
    needsTemplate?: true;
    /** The confirmation shown first, for every action without a template picker. */
    confirm?: {
      title: string;
      body: string;
      submitLabel: string;
      cancelLabel?: string;
      /** For the action that throws work away — the default blue would read as the safe choice. */
      destructive?: true;
    };
  }
> = {
  complete: {
    label: 'Complete',
    icon: CircleCheck,
    variant: 'default',
    done: 'Experiment marked as completed',
    confirm: {
      title: 'Complete Experiment',
      body: 'Are you sure you want to complete this experiment? Once completed, you’ll need to reopen the experiment to make any further changes.',
      submitLabel: 'Complete',
    },
  },
  completeAndSubmit: {
    label: 'Complete and Sign',
    icon: Signature,
    variant: 'outline',
    done: 'Experiment completed and submitted for signature',
    needsTemplate: true,
  },
  cancel: {
    label: 'Cancel',
    icon: Ban,
    variant: 'destructive',
    done: 'Experiment cancelled',
    confirm: {
      title: 'Cancel Experiment',
      body: 'Are you sure you want to cancel this experiment? Once cancelled, you’ll need to reopen the experiment to make any further changes.',
      submitLabel: 'Cancel Experiment',
      cancelLabel: 'Keep Experiment',
      destructive: true,
    },
  },
  submit: {
    label: 'Submit',
    icon: Send,
    variant: 'default',
    done: 'Experiment submitted for signature',
    needsTemplate: true,
  },
  reopen: {
    label: 'Reopen',
    icon: RotateCcw,
    variant: 'outline',
    done: 'Experiment reopened',
    confirm: {
      title: 'Reopen Experiment',
      body: 'Are you sure you want to reopen this experiment? After that, it will become editable again.',
      submitLabel: 'Reopen',
    },
  },
};

/** There is no tooltip primitive here; `Avatar` names itself with a plain `title` the same way. */
const NO_PERMISSION = 'You do not have permission to change this experiment\u2019s status';

/**
 * The workflow row at the right of the header: the transitions this experiment's status allows,
 * plus Print, which every status allows.
 *
 * **Two separate gates, in the order the backend applies them.** Which buttons exist is a question
 * about the *status* (`workflowActionsFor`, mirroring each handler's `doValidateStatus`); whether
 * they can be pressed is a question about the *user* (`canRunWorkflow`, mirroring
 * `doValidateAccess`). So an experiment out for signature shows no transitions to anybody, while a
 * collaborator without `SUBMIT_EXPERIMENTS` sees the same buttons as its owner, disabled and
 * saying why. Neither gate is `canEditExperiment` — that is `EDIT_EXPERIMENTS`, which a user can
 * hold without being allowed to move the experiment at all.
 *
 * Every button also goes inert while any write to this experiment is in flight: these all run
 * through `applyMutation` server-side, so they queue in `experimentWrite`'s scope behind the
 * on-blur field saves, and a second click would only queue a transition against a status the first
 * one is already moving.
 */
export function ExperimentActions({ experiment }: { experiment: ExperimentDetails }) {
  /**
   * The action waiting on a dialog, if any — it identifies both which dialog is open and what to
   * run when it is confirmed. One piece of state rather than a flag per dialog, since two actions
   * share the template picker and only one dialog can be open at a time.
   */
  const [pending, setPending] = useState<WorkflowAction | null>(null);
  /**
   * The action whose confirmation was last opened. Never cleared, unlike `pending`: the dialog
   * renders whether or not it is open — a Base UI popup animates shut — and reading the copy off
   * `pending` would blank the text out mid-transition.
   */
  const [confirming, setConfirming] = useState<WorkflowAction>('cancel');
  const workflow = useExperimentWorkflow(experiment.id);
  const { print, printing } = usePrintReport(experiment.id);
  const saving = useExperimentSaving(experiment.id);

  const actions = workflowActionsFor(experiment);
  const allowed = canRunWorkflow(experiment);

  /**
   * Runs the action a dialog was opened for — the dialog needs the promise. It closes itself only once the write has
   * landed; a rejection propagates into `FormDialog`, which keeps the dialog open with the choice
   * intact.
   */
  async function sendFromDialog(action: WorkflowAction, signatureTemplateId?: string) {
    await workflow.mutateAsync({ action, signatureTemplateId });
    notifyInfo(WORKFLOW[action].done);
    setPending(null);
  }

  function handleClick(action: WorkflowAction) {
    if (WORKFLOW[action].confirm) setConfirming(action);
    setPending(action);
  }

  const confirm = WORKFLOW[confirming].confirm!;

  return (
    <>
      {actions.map((action) => {
        const { label, icon: Icon, variant } = WORKFLOW[action];
        return (
          <Button
            key={action}
            variant={variant}
            size="lg"
            className="rounded-md"
            disabled={!allowed || saving}
            // Only the one that was pressed spins; the rest are simply disabled above.
            loading={workflow.isPending && workflow.variables.action === action}
            title={allowed ? undefined : NO_PERMISSION}
            onClick={() => handleClick(action)}
          >
            <Icon />
            {label}
          </Button>
        );
      })}

      {/* No status or permission gate: `printReport` reaches the experiment through
          `ExperimentRepository.load`, whose VIEW_EXPERIMENTS check the user passed to open the
          page at all. The report is generated on demand, so it is worth saying it has started. */}
      <Button
        variant="outline"
        size="icon-lg"
        aria-label="Print Report"
        loading={printing}
        onClick={() => {
          notifyInfo('Starting report generation');
          print();
        }}
      >
        <Printer />
      </Button>

      <SignDialog
        // Mounted whatever `pending` holds, so the dialog can animate shut as well as open.
        open={pending !== null && WORKFLOW[pending].needsTemplate === true}
        onOpenChange={(open) => {
          if (!open) setPending(null);
        }}
        onSubmit={(signatureTemplateId) => sendFromDialog(pending!, signatureTemplateId)}
      />

      <FormDialog
        open={pending !== null && WORKFLOW[pending].confirm !== undefined}
        onOpenChange={(open) => {
          if (!open) setPending(null);
        }}
        title={confirm.title}
        submitLabel={confirm.submitLabel}
        submitVariant={confirm.destructive ? 'destructive' : undefined}
        cancelLabel={confirm.cancelLabel}
        onSubmit={() => sendFromDialog(confirming)}
      >
        <p className="text-[14px]/6 text-neutral-800">{confirm.body}</p>
      </FormDialog>
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
 * answers 400 `Nothing to undo`. The enabled/disabled state therefore says only whether this
 * experiment is writable at all — `canEditExperiment`, the same gate every other editable surface
 * asks, and the same one `ExperimentService.mutateModel` enforces with
 * `ensureAccess(EDIT_EXPERIMENTS)`.
 *
 * Undo is also **per user** on the backend: `AbstractUndoHelper` skips revisions somebody else
 * made, so this never takes back a colleague's edit.
 */
export function UndoRedoButtons({ experiment, saving = false }: { experiment: ExperimentDetails; saving?: boolean }) {
  // `mutate`, not `mutateAsync`: nothing here awaits the result.
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
          title="Undo"
          disabled={!canEdit}
          onClick={() => send({ type: 'Undo' })}
        >
          <Undo2 />
        </Button>
        <Button
          variant="outline"
          size="icon-lg"
          aria-label="Redo"
          title="Redo"
          disabled={!canEdit}
          onClick={() => send({ type: 'Redo' })}
        >
          <Redo2 />
        </Button>
      </div>
    </SavingOverlay>
  );
}
